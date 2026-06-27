$srcPath = Join-Path $PSScriptRoot '..\magical-vibes-backend\src\main\java\com\github\laxika\magicalvibes\service\graveyard\GraveyardReturnResolutionService.java'
$normalfxDir = Join-Path $PSScriptRoot '..\magical-vibes-backend\src\main\java\com\github\laxika\magicalvibes\service\effect\normalfx'
$content = Get-Content $srcPath -Raw

$support = $content
$support = $support -replace 'package com\.github\.laxika\.magicalvibes\.service\.graveyard;', 'package com.github.laxika.magicalvibes.service.effect.normalfx;'
$support = $support -replace 'import com\.github\.laxika\.magicalvibes\.service\.effect\.normalfx\.LifeSupport;\r?\n', ''
$support = $support -replace 'import com\.github\.laxika\.magicalvibes\.service\.effect\.HandlesEffect;\r?\n', ''
$support = $support -replace '@Service', '@Component'
$support = $support -replace 'import org\.springframework\.stereotype\.Service;', 'import org.springframework.stereotype.Component;'
$support = $support -replace 'public class GraveyardReturnResolutionService', 'public class GraveyardReturnSupport'
$support = $support -replace '(?s) /\*\*[\s\S]*?Resolves all graveyard-related effects[\s\S]*?\*/\r?\n', @"
/**
 * Shared graveyard return/exile helpers used by every normal Graveyard Return effect handler
 * and by input handlers (graveyard choice, may ability pile separation).
 *
 * <p>Extracted verbatim from {@code GraveyardReturnResolutionService}; behavior is identical.
 */
"@

$pattern = '(?ms)(    /\*\*[\s\S]*?\*/\r?\n)?    @HandlesEffect\([^\)]+\)\r?\n    (?:private |public )?void [^{]+\{[\s\S]*?^    \}\r?\n'
while ($support -match $pattern) {
    $support = $support -replace $pattern, '', 1
}

$support = $support -replace '    private void ', '    public void '
$support = $support -replace '    private boolean ', '    public boolean '
$support = $support -replace '    private record ', '    public record '
$support = $support -replace '    void beginGraveyardExileChoice', '    public void beginGraveyardExileChoice'

Set-Content -Path (Join-Path $normalfxDir 'GraveyardReturnSupport.java') -Value $support -NoNewline

$handlerPattern = '(?ms)    /\*\*([\s\S]*?)\*/\r?\n    @HandlesEffect\((\w+)\.class\)\r?\n    (?:private |public )?void (\w+)\(([^\)]*)\) \{([\s\S]*?)^    \}\r?\n'
$matches = [regex]::Matches($content, $handlerPattern)
Write-Host "Found $($matches.Count) handlers"

$fieldNames = @('battlefieldEntryService','permanentRemovalService','legendRuleService','gameQueryService','gameBroadcastService','playerInputService','lifeSupport','exileService','cardViewFactory')
$helperMethods = @('resolvePreTargeted','resolvePreTargetedById','resolveReturnAll','resolveReturnAtRandom','resolveFromControllersGraveyard','resolveFromAllGraveyards','processTargetedGraveyardCards','moveCardToDestination','putCardOntoBattlefield','putCardOntoBattlefieldWithHasteAndExile','isCardBlockedFromEnteringFromZone','applyPermanentGrants','exileCardFromAnyGraveyard','handleCreatureEtbAndLegendRule','applyLifeGainEqualToManaValue','trackStolenCreature','stealFromOpponentGraveyard','createTokenCopyFromCard','beginGraveyardExileChoice','beginNextGraveyardReturnFromQueue','putCardOntoBattlefieldFromExile','buildCardPileDescription')

foreach ($m in $matches) {
    $effectClass = $m.Groups[2].Value
    $params = $m.Groups[4].Value.Trim()
    $body = $m.Groups[5].Value
    $handlerClass = $effectClass + 'Handler'

    foreach ($hm in $helperMethods) {
        $body = $body -replace "(?<![.\w])$hm\(", "graveyardReturnSupport.$hm("
    }

    $usedFields = New-Object System.Collections.Generic.List[string]
    foreach ($f in $fieldNames) {
        if ($body -match [regex]::Escape($f)) { [void]$usedFields.Add($f) }
    }
    if ($body -match 'graveyardReturnSupport') { [void]$usedFields.Add('graveyardReturnSupport') }
    $usedFields = $usedFields | Select-Object -Unique

    $depsDecl = ($usedFields | ForEach-Object { "    private final $_ $_;" }) -join "`n"

    if ($params -match '(\w+Effect) (\w+)') {
        $effectParam = $Matches[2]
        $handlerBody = "        var e = ($effectClass) effect;`n" + ($body -replace [regex]::Escape($effectParam), 'e')
    } else {
        $handlerBody = $body
    }

    $imports = [System.Collections.Generic.List[string]]::new()
    [void]$imports.Add('import com.github.laxika.magicalvibes.model.GameData;')
    [void]$imports.Add('import com.github.laxika.magicalvibes.model.StackEntry;')
    [void]$imports.Add('import com.github.laxika.magicalvibes.model.effect.CardEffect;')
    [void]$imports.Add("import com.github.laxika.magicalvibes.model.effect.$effectClass;")
    if ($usedFields -contains 'lifeSupport') { [void]$imports.Add('import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;') }
    if ($usedFields -contains 'battlefieldEntryService') { [void]$imports.Add('import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;') }
    if ($usedFields -contains 'permanentRemovalService') { [void]$imports.Add('import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;') }
    if ($usedFields -contains 'legendRuleService') { [void]$imports.Add('import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;') }
    if ($usedFields -contains 'gameQueryService') { [void]$imports.Add('import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;') }
    if ($usedFields -contains 'gameBroadcastService') { [void]$imports.Add('import com.github.laxika.magicalvibes.service.GameBroadcastService;') }
    if ($usedFields -contains 'playerInputService') { [void]$imports.Add('import com.github.laxika.magicalvibes.service.input.PlayerInputService;') }
    if ($usedFields -contains 'exileService') { [void]$imports.Add('import com.github.laxika.magicalvibes.service.exile.ExileService;') }
    if ($usedFields -contains 'cardViewFactory') { [void]$imports.Add('import com.github.laxika.magicalvibes.networking.service.CardViewFactory;') }

    $handler = @"
package com.github.laxika.magicalvibes.service.effect.normalfx;

$($imports -join "`n")

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class $handlerClass implements NormalEffectHandlerBean {

$depsDecl

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return $effectClass.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
$handlerBody    }
}
"@

    Set-Content -Path (Join-Path $normalfxDir "$handlerClass.java") -Value $handler -NoNewline
    Write-Host "Created $handlerClass"
}
