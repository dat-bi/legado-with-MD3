param(
    [string]$Root = "c:\Users\Admin\Downloads\legado-with-MD3",
    [string]$Endpoint = "http://14.225.254.182/index.php?ngmar=trans&langhint=chinese",
    [int]$MaxChars = 3500,
    [int]$DelayMs = 600
)

$ErrorActionPreference = "Stop"

function Split-ByMaxChars([string]$text, [int]$maxChars) {
    $chunks = New-Object System.Collections.Generic.List[string]
    $current = ""
    foreach ($line in $text -split "`n") {
        $candidate = if ($current.Length -eq 0) { $line } else { "$current`n$line" }
        if ($candidate.Length -gt $maxChars -and $current.Length -gt 0) {
            $chunks.Add($current)
            $current = $line
        } else {
            $current = $candidate
        }
    }
    if ($current.Length -gt 0) { $chunks.Add($current) }
    return $chunks
}

function Call-Translate([string]$text) {
    if ([string]::IsNullOrWhiteSpace($text)) { return $text }
    $form = New-Object System.Collections.Generic.Dictionary[string, string]
    $form["ajax"] = "trans"
    $form["content"] = $text
    $form["nonodes"] = "true"
    try {
        $resp = Invoke-RestMethod -Uri $Endpoint -Method Post -Body $form -ContentType "application/x-www-form-urlencoded"
    } catch {
        Write-Host "API Error: $_"
        return $text
    }
    if (-not $resp) { return $text }
    $result = $resp.ToString().Split('|||').Join("`n").Trim()
    Write-Host "Translated snippet: $($result.Substring(0, [Math]::Min(100, $result.Length)))"
    return $result
}

$mdFiles = Get-ChildItem -Path $Root -Recurse -Filter "*.md" -File |
    Where-Object { $_.FullName -notmatch "\\build\\" }

Write-Host "Found $($mdFiles.Count) md files."

foreach ($file in $mdFiles) {
    Write-Host "Translating: $($file.FullName)"
    $content = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    if ([string]::IsNullOrWhiteSpace($content)) { continue }

    $chunks = Split-ByMaxChars $content $MaxChars
    $translatedChunks = New-Object System.Collections.Generic.List[string]
    for ($i = 0; $i -lt $chunks.Count; $i++) {
        $translatedChunks.Add((Call-Translate $chunks[$i]))
        if ($i -lt $chunks.Count - 1) { Start-Sleep -Milliseconds $DelayMs }
    }

    $final = ($translatedChunks -join "`n")
    Set-Content -LiteralPath $file.FullName -Value $final -NoNewline -Encoding UTF8
}

Write-Host "Done."