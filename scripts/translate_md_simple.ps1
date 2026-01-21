param(
    [string]$Root = "c:\Users\Admin\Downloads\legado-with-MD3",
    [int]$MaxChars = 3500,
    [int]$DelayMs = 600
)

$ErrorActionPreference = "Stop"

function Split-ByMaxChars([string]$text, [int]$maxChars) {
    $chunks = @()
    $current = ""
    foreach ($line in $text -split "`n") {
        $candidate = if ($current.Length -eq 0) { $line } else { "$current`n$line" }
        if ($candidate.Length -gt $maxChars -and $current.Length -gt 0) {
            $chunks += $current
            $current = $line
        }
        else {
            $current = $candidate
        }
    }
    if ($current.Length -gt 0) { $chunks += $current }
    return $chunks
}

function Translate-Text([string]$text) {
    if ([string]::IsNullOrWhiteSpace($text)) { return $text }
    $body = "ajax=trans&content=[System.Uri]::EscapeDataString($text)&nonodes=true"
    try {
        $resp = Invoke-RestMethod -Uri "http://14.225.254.182/index.php?ngmar=trans&langhint=chinese" -Method Post -Body $body -ContentType "application/x-www-form-urlencoded"
        if ($resp) { return $resp.ToString().Split('|||').Join("`n").Trim() }
    }
    catch { }
    return $text
}

$mdFiles = Get-ChildItem -Path $Root -Recurse -Filter "*.md" -File | Where-Object { $_.FullName -notmatch "\\build\\" }
Write-Host "Found $($mdFiles.Count) md files."

foreach ($file in $mdFiles) {
    Write-Host "Translating: $($file.FullName)"
    $content = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    if ([string]::IsNullOrWhiteSpace($content)) { continue }
    $chunks = Split-ByMaxChars $content $MaxChars
    $translated = @()
    for ($i = 0; $i -lt $chunks.Count; $i++) {
        $translated += Translate-Text $chunks[$i]
        if ($i -lt $chunks.Count - 1) { Start-Sleep -Milliseconds $DelayMs }
    }
    $final = $translated -join "`n"
    Set-Content -LiteralPath $file.FullName -Value $final -NoNewline -Encoding UTF8
}

Write-Host "Done."
