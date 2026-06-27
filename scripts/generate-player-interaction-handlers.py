#!/usr/bin/env python3
"""Generate PlayerInteractionSupport + normalfx handlers from PlayerInteractionResolutionService."""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SERVICE_PATH = ROOT / "magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/PlayerInteractionResolutionService.java"
SUPPORT_PATH = ROOT / "magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/normalfx/PlayerInteractionSupport.java"
HANDLER_DIR = ROOT / "magical-vibes-backend/src/main/java/com/github/laxika/magicalvibes/service/effect/normalfx"

HELPER_METHODS = [
    "applyOpponentMayPlayCreature",
    "applyPutCardToBattlefield",
    "resolvePlayerMayPlayCreature",
    "applyDrawCards",
    "resolveDiscardCards",
    "resolveRandomDiscardCards",
    "resolveHandRevealAndChoose",
    "sharesCardType",
    "mapCardTypeToSpellType",
    "startNextEachPlayerDiscard",
]

# Handler -> extra field dependencies beyond support + common
HANDLER_EXTRA_DEPS: dict[str, list[str]] = {
    "FlipCoinWinEffect": ["EffectHandlerRegistry effectHandlerRegistry", "GameBroadcastService gameBroadcastService"],
    "FlipTwoCoinsEffect": ["EffectHandlerRegistry effectHandlerRegistry", "GameBroadcastService gameBroadcastService"],
    "SacrificeSelfAndDrawCardsEffect": ["PermanentRemovalService permanentRemovalService", "GameBroadcastService gameBroadcastService", "GameQueryService gameQueryService"],
    "SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect": ["PermanentRemovalService permanentRemovalService", "GameBroadcastService gameBroadcastService"],
    "DrawXCardsForTargetPlayerEffect": ["DrawService drawService", "GameBroadcastService gameBroadcastService"],
    "ShuffleHandIntoLibraryAndDrawEffect": ["DrawService drawService", "GameBroadcastService gameBroadcastService"],
    "DrawCardsEqualToChargeCountersOnSourceEffect": ["DrawService drawService", "GameBroadcastService gameBroadcastService"],
    "DrawCardsEqualToControlledCreatureCountEffect": ["DrawService drawService", "GameBroadcastService gameBroadcastService", "GameQueryService gameQueryService"],
    "DiscardOwnHandEffect": ["GraveyardService graveyardService", "GameBroadcastService gameBroadcastService", "TriggerCollectionService triggerCollectionService"],
    "DiscardOwnHandThenDrawThatManyEffect": ["DrawService drawService", "GraveyardService graveyardService", "GameBroadcastService gameBroadcastService", "TriggerCollectionService triggerCollectionService"],
    "DrawDiscardTransformIfCreatureDiscardedEffect": ["DrawService drawService"],
    "RegisterDelayedCombatDamageLootEffect": ["GameBroadcastService gameBroadcastService"],
    "DiscardUnlessExileCardFromGraveyardEffect": ["GameQueryService gameQueryService"],
    "DiscardUpToThenDrawThatManyEffect": ["PlayerInputService playerInputService", "GameBroadcastService gameBroadcastService"],
    "TargetPlayerExilesFromHandEffect": ["PlayerInputService playerInputService", "GameBroadcastService gameBroadcastService", "GameQueryService gameQueryService"],
    "TargetSpellControllerDiscardsEffect": [],
    "TargetPlayerDiscardsByChargeCountersEffect": ["GameBroadcastService gameBroadcastService"],
    "ChooseCardNameAndExileFromZonesEffect": ["PlayerInputService playerInputService"],
    "ExileTargetGraveyardCardAndSameNameFromZonesEffect": ["PlayerInputService playerInputService", "GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "DrawAndRandomDiscardWithSharedTypeCountersEffect": ["DrawService drawService", "GraveyardService graveyardService", "GameBroadcastService gameBroadcastService", "GameQueryService gameQueryService", "TriggerCollectionService triggerCollectionService"],
    "TargetPlayerRandomDiscardOrControllerDrawsEffect": ["DrawService drawService", "GameBroadcastService gameBroadcastService"],
    "ReturnPermanentsOnCombatDamageToPlayerEffect": ["PlayerInputService playerInputService", "GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "PutAwakeningCountersOnTargetLandsEffect": ["PlayerInputService playerInputService", "GameBroadcastService gameBroadcastService", "GameQueryService gameQueryService"],
    "MayEffect": [],
    "MayPayManaEffect": [],
    "SacrificeArtifactThenDealDividedDamageEffect": ["PlayerInputService playerInputService", "GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "SacrificePermanentThenEffect": ["PlayerInputService playerInputService", "GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "SacrificeCreatureToCreateTokensEqualToToughnessEffect": ["PlayerInputService playerInputService", "GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "LookAtHandEffect": ["SessionManager sessionManager", "CardViewFactory cardViewFactory", "GameBroadcastService gameBroadcastService"],
    "RevealRandomCardFromTargetPlayerHandEffect": ["SessionManager sessionManager", "CardViewFactory cardViewFactory", "GameBroadcastService gameBroadcastService"],
    "ChooseCardsFromTargetHandToTopOfLibraryEffect": ["PlayerInputService playerInputService", "GameBroadcastService gameBroadcastService"],
    "ChangeColorTextEffect": ["SessionManager sessionManager", "GameQueryService gameQueryService"],
    "AwardAnyColorManaWithInstantSorceryCopyEffect": ["SessionManager sessionManager"],
    "AwardAnyColorManaEffect": ["SessionManager sessionManager"],
    "AddManaPerAttackingCreatureEffect": ["SessionManager sessionManager"],
    "DrawAndLoseLifePerSubtypeEffect": ["DrawService drawService", "GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "RedirectDrawsEffect": ["GameBroadcastService gameBroadcastService"],
    "DrawCardForTargetPlayerEffect": ["DrawService drawService", "GameQueryService gameQueryService"],
    "SacrificeUnlessDiscardCardTypeEffect": ["PermanentRemovalService permanentRemovalService", "GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "LoseLifeUnlessDiscardEffect": ["GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "LoseLifeUnlessPaysEffect": ["GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService"],
    "SacrificeUnlessReturnOwnPermanentTypeToHandEffect": ["PermanentRemovalService permanentRemovalService", "GameBroadcastService gameBroadcastService"],
    "GrantPermanentNoMaxHandSizeEffect": ["GameBroadcastService gameBroadcastService"],
    "RevealRandomHandCardAndPlayEffect": ["SessionManager sessionManager", "CardViewFactory cardViewFactory", "BattlefieldEntryService battlefieldEntryService", "GameQueryService gameQueryService", "GameBroadcastService gameBroadcastService", "TriggerCollectionService triggerCollectionService", "PlayerInputService playerInputService"],
}


HELPER_RETURN_TYPES = {
    "sharesCardType": "boolean",
    "mapCardTypeToSpellType": "StackEntryType",
}


def extract_method_body(src: str, method_name: str) -> str:
    """Extract method body starting from method signature containing method_name."""
    ret = HELPER_RETURN_TYPES.get(method_name, "void")
    pattern = rf'(?:public |private )?{ret}\s+{re.escape(method_name)}\s*\('
    m = re.search(pattern, src)
    if not m:
        raise ValueError(f"Method {method_name} not found")
    start = m.start()
    # Find opening brace
    brace = src.find("{", m.end())
    depth = 0
    i = brace
    while i < len(src):
        if src[i] == "{":
            depth += 1
        elif src[i] == "}":
            depth -= 1
            if depth == 0:
                return src[brace + 1 : i]
        i += 1
    raise ValueError(f"Unclosed brace for {method_name}")


def extract_handler_body(src: str, effect_class: str) -> tuple[str, str, str]:
    """Return (method_name, params_after_gameData_entry, body)."""
    pattern = rf'@HandlesEffect\({effect_class}\.class\)\s+void\s+(\w+)\(([^)]*)\)\s*\{{'
    m = re.search(pattern, src)
    if not m:
        raise ValueError(f"Handler for {effect_class} not found")
    method_name = m.group(1)
    params = m.group(2)
    body = extract_method_body(src, method_name)
    return method_name, params, body


def transform_body(body: str) -> str:
  replacements = [
      ("applyOpponentMayPlayCreature(", "playerInteractionSupport.applyOpponentMayPlayCreature("),
      ("applyPutCardToBattlefield(", "playerInteractionSupport.applyPutCardToBattlefield("),
      ("resolvePlayerMayPlayCreature(", "playerInteractionSupport.resolvePlayerMayPlayCreature("),
      ("applyDrawCards(", "playerInteractionSupport.applyDrawCards("),
      ("resolveDiscardCards(", "playerInteractionSupport.resolveDiscardCards("),
      ("resolveRandomDiscardCards(", "playerInteractionSupport.resolveRandomDiscardCards("),
      ("resolveHandRevealAndChoose(", "playerInteractionSupport.resolveHandRevealAndChoose("),
      ("sharesCardType(", "playerInteractionSupport.sharesCardType("),
      ("mapCardTypeToSpellType(", "playerInteractionSupport.mapCardTypeToSpellType("),
      ("startNextEachPlayerDiscard(", "playerInteractionSupport.startNextEachPlayerDiscard("),
  ]
  for old, new in replacements:
      body = body.replace(old, new)
  return body


def build_support(src: str) -> str:
    methods_code = []
    for name in HELPER_METHODS:
        body = extract_method_body(src, name)
        # Get full signature from source
        ret = HELPER_RETURN_TYPES.get(name, "void")
        pattern = rf'((?:public |private )?{ret}\s+{re.escape(name)}\s*\([^)]*\))\s*\{{'
        m = re.search(pattern, src)
        sig = m.group(1).replace("private ", "public ")
        methods_code.append(f"    {sig} {{\n{body}\n    }}")

    return f"""package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Shared draw/discard/reveal/choice helpers used by every PlayerInteraction effect handler
 * and by input services (e.g. CardChoiceHandlerService).
 *
 * <p>Extracted verbatim from PlayerInteractionResolutionService; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerInteractionSupport {{

    private final DrawService drawService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final TriggerCollectionService triggerCollectionService;

{chr(10).join(methods_code)}
}}
"""


def handler_class_name(effect_class: str) -> str:
    return effect_class.replace("Effect", "EffectHandler")


def build_handler(effect_class: str, params: str, body: str) -> str:
    handler_name = handler_class_name(effect_class)
    body = transform_body(body)

    extra_deps = HANDLER_EXTRA_DEPS.get(effect_class, [])
    # Always include support; add extras deduped
    field_types = {"PlayerInteractionSupport playerInteractionSupport"}
    field_types.update(extra_deps)

    # Determine if effect param exists
    has_effect_param = "Effect effect" in params or "Effect " in params
    effect_var = "effect"
    if has_effect_param:
        for part in params.split(","):
            part = part.strip()
            if "Effect" in part:
                effect_var = part.split()[-1]
                break

    cast_line = ""
    if has_effect_param and effect_var != "effect":
        cast_line = f"        var effect = ({effect_class}) {effect_var};\n"

    resolve_params = "GameData gameData, StackEntry entry, CardEffect effect"
    cast = f"        var e = ({effect_class}) effect;\n" if has_effect_param else ""

    # Replace effect field references in body when using 'e'
    resolved_body = body
    if has_effect_param:
        for part in params.split(","):
            part = part.strip()
            if "Effect" in part:
                var = part.split()[-1]
                if var != "effect":
                    resolved_body = resolved_body.replace(f"{var}.", "e.")
                break
        resolved_body = re.sub(r'\beffect\.', 'e.', resolved_body)
        resolved_body = re.sub(r'\beffect\)', 'e)', resolved_body)
        resolved_body = re.sub(r',\s*effect,', ', e,', resolved_body)

    # Auto-detect missing service field references
    field_dep_map = {
        "drawService": "DrawService drawService",
        "graveyardService": "GraveyardService graveyardService",
        "gameQueryService": "GameQueryService gameQueryService",
        "gameBroadcastService": "GameBroadcastService gameBroadcastService",
        "playerInputService": "PlayerInputService playerInputService",
        "sessionManager": "SessionManager sessionManager",
        "cardViewFactory": "CardViewFactory cardViewFactory",
        "permanentRemovalService": "PermanentRemovalService permanentRemovalService",
        "battlefieldEntryService": "BattlefieldEntryService battlefieldEntryService",
        "triggerCollectionService": "TriggerCollectionService triggerCollectionService",
        "effectHandlerRegistry": "EffectHandlerRegistry effectHandlerRegistry",
    }
    for field, dep in field_dep_map.items():
        if f"{field}." in resolved_body:
            field_types.add(dep)

    fields = sorted(field_types, key=lambda x: x.split()[-1])
    field_decls = "\n".join(f"    private final {f};" for f in fields)

    imports = set([
        "com.github.laxika.magicalvibes.model.GameData",
        "com.github.laxika.magicalvibes.model.StackEntry",
        "com.github.laxika.magicalvibes.model.effect.CardEffect",
        f"com.github.laxika.magicalvibes.model.effect.{effect_class}",
        "lombok.RequiredArgsConstructor",
        "org.springframework.stereotype.Component",
    ])

    extra_import_map = {
        "DrawService": "com.github.laxika.magicalvibes.service.DrawService",
        "GraveyardService": "com.github.laxika.magicalvibes.service.graveyard.GraveyardService",
        "GameQueryService": "com.github.laxika.magicalvibes.service.battlefield.GameQueryService",
        "GameBroadcastService": "com.github.laxika.magicalvibes.service.GameBroadcastService",
        "PlayerInputService": "com.github.laxika.magicalvibes.service.input.PlayerInputService",
        "SessionManager": "com.github.laxika.magicalvibes.networking.SessionManager",
        "CardViewFactory": "com.github.laxika.magicalvibes.networking.service.CardViewFactory",
        "PermanentRemovalService": "com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService",
        "BattlefieldEntryService": "com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService",
        "TriggerCollectionService": "com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService",
        "EffectHandlerRegistry": "com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry",
    }

    for dep in field_types:
        type_name = dep.split()[0]
        if type_name in extra_import_map:
            imports.add(extra_import_map[type_name])

    scan_body = resolved_body
    if "log." in scan_body:
        imports.add("lombok.extern.slf4j.Slf4j")
    if "List." in scan_body or "List<" in scan_body:
        imports.add("java.util.List")
    if "ThreadLocalRandom" in scan_body:
        imports.add("java.util.concurrent.ThreadLocalRandom")
    if "PendingMayAbility" in scan_body or "new PendingMayAbility" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.PendingMayAbility")
    if "PendingTransformOnCreatureDiscard" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.PendingTransformOnCreatureDiscard")
    if "PendingReturnToHandOnDiscardType" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.PendingReturnToHandOnDiscardType")
    if "PermanentChoiceContext" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.PermanentChoiceContext")
    if "ChooseFromListMessage" in scan_body:
        imports.add("com.github.laxika.magicalvibes.networking.message.ChooseFromListMessage")
    if "RevealHandMessage" in scan_body:
        imports.add("com.github.laxika.magicalvibes.networking.message.RevealHandMessage")
    if "CardView" in scan_body:
        imports.add("com.github.laxika.magicalvibes.networking.model.CardView")
    if "ChoiceContext" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.ChoiceContext")
    if "CardPredicateUtils" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.filter.CardPredicateUtils")
    if "CounterType" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.CounterType")
    if "EffectHandler" in scan_body:
        imports.add("com.github.laxika.magicalvibes.service.effect.EffectHandler")
    if "CardType" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.CardType")
    if "Permanent " in scan_body or "Permanent\n" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.Permanent")
    if "Card>" in scan_body or "Card " in scan_body or "Card::" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.Card")
    if "EffectResolution" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.EffectResolution")
    if "EffectSlot" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.EffectSlot")
    if "TargetType" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.TargetType")
    if "StackEntryType" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.StackEntryType")
    if "PermanentPredicateTargetFilter" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter")
    if "LibraryShuffleHelper" in scan_body:
        imports.add("com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper")
    if "ManaCost" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.ManaCost")
    if "ManaPool" in scan_body:
        imports.add("com.github.laxika.magicalvibes.model.ManaPool")
    if "ArrayList" in scan_body:
        imports.add("java.util.ArrayList")
    if "Map<" in scan_body:
        imports.add("java.util.Map")
    if "Set." in scan_body or "Set<" in scan_body or "Set.copyOf" in scan_body:
        imports.add("java.util.Set")
    if "UUID" in scan_body:
        imports.add("java.util.UUID")

    slf4j = "@Slf4j\n" if "lombok.extern.slf4j.Slf4j" in imports else ""

    import_block = "\n".join(f"import {i};" for i in sorted(imports))

    return f"""package com.github.laxika.magicalvibes.service.effect.normalfx;

{import_block}

{slf4j}@Component
@RequiredArgsConstructor
public class {handler_name} implements NormalEffectHandlerBean {{

{field_decls}

    @Override
    public Class<? extends CardEffect> handledEffect() {{
        return {effect_class}.class;
    }}

    @Override
    public void resolve({resolve_params}) {{
{cast}{resolved_body}
    }}
}}
"""


def factory_entry(effect_class: str, params: str, body: str) -> str:
    handler = handler_class_name(effect_class)
    body = transform_body(body)
    extra_deps = HANDLER_EXTRA_DEPS.get(effect_class, [])
    field_types = {"PlayerInteractionSupport playerInteractionSupport"}
    field_types.update(extra_deps)

    resolved_body = body
    if "Effect" in params:
        for part in params.split(","):
            part = part.strip()
            if "Effect" in part:
                var = part.split()[-1]
                if var != "effect":
                    resolved_body = resolved_body.replace(f"{var}.", "e.")
                break
        resolved_body = re.sub(r'\beffect\.', 'e.', resolved_body)
        resolved_body = re.sub(r'\beffect\)', 'e)', resolved_body)
        resolved_body = re.sub(r',\s*effect,', ', e,', resolved_body)

    field_dep_map = {
        "drawService": "DrawService drawService",
        "graveyardService": "GraveyardService graveyardService",
        "gameQueryService": "GameQueryService gameQueryService",
        "gameBroadcastService": "GameBroadcastService gameBroadcastService",
        "playerInputService": "PlayerInputService playerInputService",
        "sessionManager": "SessionManager sessionManager",
        "cardViewFactory": "CardViewFactory cardViewFactory",
        "permanentRemovalService": "PermanentRemovalService permanentRemovalService",
        "battlefieldEntryService": "BattlefieldEntryService battlefieldEntryService",
        "triggerCollectionService": "TriggerCollectionService triggerCollectionService",
        "effectHandlerRegistry": "EffectHandlerRegistry effectHandlerRegistry",
    }
    for field, dep in field_dep_map.items():
        if f"{field}." in resolved_body:
            field_types.add(dep)

    args = [dep.split()[-1] for dep in sorted(field_types, key=lambda x: x.split()[-1])]
    return f"                new {handler}({', '.join(args)})"


def main():
    src = SERVICE_PATH.read_text(encoding="utf-8")

    # Support
    support = build_support(src)
    SUPPORT_PATH.write_text(support, encoding="utf-8")
    print(f"Wrote {SUPPORT_PATH}")

    # Handlers
    pattern = r'@HandlesEffect\((\w+)\.class\)'
    effects = [m.group(1) for m in re.finditer(pattern, src)]
    factory_lines = []
    for effect_class in effects:
        _, params, body = extract_handler_body(src, effect_class)
        handler_src = build_handler(effect_class, params, body)
        path = HANDLER_DIR / f"{handler_class_name(effect_class)}.java"
        path.write_text(handler_src, encoding="utf-8")
        factory_lines.append(factory_entry(effect_class, params, body))
        print(f"Wrote {path.name}")

    factory_out = ROOT / "scripts/player-interaction-factory-entries.txt"
    factory_out.write_text(",\n".join(factory_lines), encoding="utf-8")
    print(f"Wrote {len(effects)} handlers, factory entries at {factory_out}")


if __name__ == "__main__":
    main()
