# Firebase Configuration Verification Script
# Run this after downloading google-services.json from Firebase Console

$firebaseConfigPath = "app\google-services.json"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Firebase Configuration Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path $firebaseConfigPath)) {
    Write-Host "❌ ERROR: google-services.json not found at: $firebaseConfigPath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please download google-services.json from Firebase Console and place it at:" -ForegroundColor Yellow
    Write-Host "  $firebaseConfigPath" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ File found: $firebaseConfigPath" -ForegroundColor Green
Write-Host ""

try {
    $config = Get-Content $firebaseConfigPath | ConvertFrom-Json
    
    # Check package name
    $packageName = $config.client[0].client_info.android_client_info.package_name
    Write-Host "Package Name: $packageName" -ForegroundColor $(if ($packageName -eq "gpslink.traccar.manager") { "Green" } else { "Red" })
    
    if ($packageName -ne "gpslink.traccar.manager") {
        Write-Host "⚠️  WARNING: Package name mismatch!" -ForegroundColor Yellow
        Write-Host "   Expected: gpslink.traccar.manager" -ForegroundColor Yellow
        Write-Host "   Found:    $packageName" -ForegroundColor Yellow
    }
    
    # Check project info
    $projectId = $config.project_info.project_id
    $projectNumber = $config.project_info.project_number
    
    Write-Host ""
    Write-Host "Project ID: $projectId" -ForegroundColor Cyan
    Write-Host "Project Number: $projectNumber" -ForegroundColor Cyan
    
    # Check if it's still placeholder
    if ($projectNumber -eq "000000000000" -or $projectId -eq "traccar-manager-android") {
        Write-Host ""
        Write-Host "⚠️  WARNING: This appears to be a placeholder file!" -ForegroundColor Yellow
        Write-Host "   Please download the actual google-services.json from Firebase Console" -ForegroundColor Yellow
    } else {
        Write-Host ""
        Write-Host "✅ Configuration looks valid!" -ForegroundColor Green
    }
    
    # Check API key
    $apiKey = $config.client[0].api_key[0].current_key
    if ($apiKey -like "*REPLACE*" -or $apiKey.Length -lt 20) {
        Write-Host ""
        Write-Host "⚠️  WARNING: API key appears to be invalid or placeholder" -ForegroundColor Yellow
    } else {
        Write-Host "✅ API Key: Present" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Verification Complete" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    
} catch {
    Write-Host ""
    Write-Host "❌ ERROR: Invalid JSON file" -ForegroundColor Red
    Write-Host "   $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
