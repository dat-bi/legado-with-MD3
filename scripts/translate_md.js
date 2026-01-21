const fs = require('fs');
const path = require('path');
const http = require('http');
const querystring = require('querystring');

const root = process.argv[2] || 'c:\\Users\\Admin\\Downloads\\legado-with-MD3';
const endpoint = 'http://14.225.254.182/index.php?ngmar=trans&langhint=chinese';
const maxChars = 3500;
const delayMs = 600;

function splitByMaxChars(text, max) {
    const chunks = [];
    let current = '';
    for (const line of text.split('\n')) {
        const candidate = current ? current + '\n' + line : line;
        if (candidate.length > max && current.length > 0) {
            chunks.push(current);
            current = line;
        } else {
            current = candidate;
        }
    }
    if (current) chunks.push(current);
    return chunks;
}

function translate(text) {
    return new Promise((resolve) => {
        const postData = querystring.stringify({
            ajax: 'trans',
            content: text,
            nonodes: 'true'
        });
        const options = {
            hostname: '14.225.254.182',
            port: 80,
            path: '/index.php?ngmar=trans&langhint=chinese',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Content-Length': Buffer.byteLength(postData)
            }
        };
        const req = http.request(options, (res) => {
            let data = '';
            res.on('data', (chunk) => data += chunk);
            res.on('end', () => {
                const result = data.split('|||').join('\n').trim();
                console.log('Translated snippet:', result.substring(0, 100));
                resolve(result);
            });
        });
        req.on('error', (e) => {
            console.error('Request error:', e);
            resolve(text);
        });
        req.write(postData);
        req.end();
    });
}

function walk(dir) {
    const files = [];
    try {
        const entries = fs.readdirSync(dir, { withFileTypes: true });
        for (const entry of entries) {
            const p = path.join(dir, entry.name);
            if (entry.isDirectory() && !p.includes('\\build\\')) {
                files.push(...walk(p));
            } else if (entry.isFile() && entry.name.endsWith('.md')) {
                files.push(p);
            }
        }
    } catch (e) {
        console.error('Error reading dir', dir, e);
    }
    return files;
}

async function main() {
    try {
        const mdFiles = walk(root);
        console.log(`Found ${mdFiles.length} md files.`);
        for (const file of mdFiles) {
            console.log(`Translating: ${file}`);
            try {
                const content = fs.readFileSync(file, 'utf8');
                if (!content.trim()) continue;
                const chunks = splitByMaxChars(content, maxChars);
                const translated = [];
                for (let i = 0; i < chunks.length; i++) {
                    translated.push(await translate(chunks[i]));
                    if (i < chunks.length - 1) await new Promise(r => setTimeout(r, delayMs));
                }
                fs.writeFileSync(file, translated.join('\n'), 'utf8');
            } catch (e) {
                console.error('Error processing file', file, e);
            }
        }
        console.log('Done.');
    } catch (e) {
        console.error('Main error', e);
    }
}

main().catch(console.error);