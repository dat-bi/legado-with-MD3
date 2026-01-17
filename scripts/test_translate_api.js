// Quick API test script for translation providers
// Usage: node scripts/test_translate_api.js "input text"

const input = process.argv.slice(2).join(" ").trim();
if (!input) {
    console.error("Usage: node scripts/test_translate_api.js \"input text\"");
    process.exit(1);
}

async function dichngay(text) {
    const url = "https://dichngay.com/translate/text";
    const payload = {
        content: JSON.stringify([text]),
        tl: "vi",
    };
    const res = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            referer: "https://dichngay.com/",
        },
        body: JSON.stringify(payload),
    });
    if (!res.ok) return null;
    const json = await res.json();
    const contentStr = json?.data?.content || json?.translatedText;
    if (!contentStr) return null;
    try {
        return JSON.parse(contentStr)?.[0] ?? null;
    } catch {
        return null;
    }
}

async function dichnhanh(text) {
    const url = "https://api.dichnhanh.com/";
    const form = new URLSearchParams();
    form.set("type", "Ancient");
    form.set("enable_analyze", "0");
    form.set("enable_fanfic", "0");
    form.set("mode", "vi");
    form.set("text", JSON.stringify(text));
    form.set("remove", "");
    const res = await fetch(url, {
        method: "POST",
        headers: {
            "Accept": "application/json, text/plain, */*",
            "Content-Type": "application/x-www-form-urlencoded",
            referer: "https://dichnhanh.com/",
        },
        body: form.toString(),
    });
    if (!res.ok) return null;
    const json = await res.json();
    if (!json?.success) return null;
    const contentStr = json?.data?.content;
    if (!contentStr) return null;
    const sanitized = contentStr
        .replace(/\u0000-\u001F/g, "")
        .replace(/\\"/g, '"')
        .replace(/'/g, '"')
        .trim();
    try {
        const arr = JSON.parse(`[${sanitized}]`);
        return arr?.[0] ?? null;
    } catch {
        return null;
    }
}

async function sangtacviet(text) {
    const QTurl = "http://14.225.254.182/index.php?ngmar=trans&langhint=chinese";
    const body = new URLSearchParams();
    body.set("ajax", "trans");
    body.set("content", text);
    body.set("nonodes", "true");
    const res = await fetch(QTurl, {
        method: "POST",
        headers: {
            "Content-type": "application/x-www-form-urlencoded",
        },
        body: body.toString(),
    });
    if (!res.ok) return null;
    const data = await res.text();
    return data.split("|||").join("\n").trim();
}

(async () => {
    console.log("Input:", input);
    console.log("- dichngay:", await dichngay(input));
    console.log("- dichnhanh:", await dichnhanh(input));
    console.log("- sangtacviet:", await sangtacviet(input));
})();
