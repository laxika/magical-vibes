#!/usr/bin/env python3
"""Step 15, run 17z11: the "done" audit's scan of the services `step15-run17z6-audit.py` doesn't parse.

Run 17z6's audit reads only the `@Collects...Trigger` handlers of `service/trigger/`. The combat services, the step and
enter services and `TriggerCollectionService` itself put triggered abilities on the stack with no such handler, and
read card effects by their class there too. This scan finds every effect-class read in those files
(`instanceof XEffect`, `XEffect.class::isInstance`) and every `getOpponentId` call, and reports each by the
classification run 17z11 made of it by hand (`CLASSIFIED`, keyed by file and effect class):

- a row of step 15 (`17z11b` ...): the class decides event data put on the entry or the target choice (its target,
  source, event value, triggering player or permanent, xValue, damage source, nonTargeting), or rebuilds or unwraps the
  card's effect;
- `ROUTE`: the class only picks the queue, choice or prompt the ability goes through, or is a trigger condition
  (step 16 deletes these with the collectors);
- `STATIC`: no triggered ability (combat legality, prevention, replacement, as-enters choices, mana resolved at once);
- `ENGINE`: an effect the engine makes itself.

A read with no entry is `UNCLASSIFIED`, with the window heuristic's guess (BIND, REBUILD, UNWRAP or ROUTE): a new read,
or one a later run moved, to classify. The step's "done" check wants no read in an open row and none unclassified.

Read-only. Usage: step15-run17z11-audit.py [--summary]
"""
import pathlib
import re
import sys
from collections import Counter

ROOT = pathlib.Path(__file__).resolve().parent.parent
S = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"
FILES = [
    "combat/attack/CombatAttackService.java", "combat/CombatDamageService.java", "combat/CombatTriggerService.java",
    "combat/block/CombatBlockService.java", "combat/CombatHelper.java", "combat/CombatService.java",
    "combat/attack/AttackLegalityService.java", "combat/block/BlockLegalityService.java",
    "battlefield/EtbTriggerService.java", "turn/StepTriggerService.java", "trigger/TriggerCollectionService.java",
]
HIT = re.compile(r"instanceof (?:[\w.]+\.)?(\w+Effect)\b|(\w+Effect)\.class::isInstance")
WRAPPERS = {"MayEffect", "ConditionalEffect", "SequenceEffect", "MayPayManaEffect", "MayPayLifeEffect"}
BIND = re.compile(r"\.set(TargetId|EventValue|SourcePermanentId|TriggeringPlayerId|TriggeringPermanentId|NonTargeting"
                  r"|XValue|DamageSource\w*|EventDamageSource|TargetCardIds)\(|new StackEntry\(|queue\w*MayAbility\w*\(")
REBUILD = re.compile(r"new (?!MayEffect\(|ArrayList|StackEntry|Permanent\()\w+Effect\(")
UNWRAP = re.compile(r"\.(wrapped|thenEffect|elseEffect|tokenEffect|innerEffect|resolvedEffects?|effects|choice)\(\)")

# Rows of step 15 (effect-slot-refactor.md, run 17z11's Findings).
B, C, C2, D, E, E2, E3, F = "17z11b", "17z11c", "17z11c2", "17z11d", "17z11e", "17z11e2", "17z11e3", "17z11f"
F2, F3 = "17z11f2", "17z11f3"
LEGALITY = ("CombatHelper.java", "CombatService.java", "AttackLegalityService.java", "BlockLegalityService.java")

CLASSIFIED = {
    # 17z11b: the damage-to-player trigger context, a capability interface, and the attached combat damage readers.
    ("CombatDamageService.java", "CombatDamageTriggerContextEffect"): B,
    ("CombatDamageService.java", "CreateTokenForTriggeringPlayerEffect"): B,
    ("CombatDamageService.java", "DealDamageToPlayersEffect"): B,
    ("CombatTriggerService.java", "CreateTokenForTriggeringPlayerEffect"): B,
    ("CombatTriggerService.java", "EquippedCreatureDealsDamageToDefendingPlayerEffect"): B,
    ("TriggerCollectionService.java", "CombatDamageTriggerContextEffect"): B,
    # 17z11c: the combat opponent and the blocked or blocking creature by the effect's class.
    ("CombatTriggerService.java", "CombatOpponentReferencingEffect"): C,
    ("CombatTriggerService.java", "DestroyCombatOpponentAtEndOfCombatEffect"): C,
    ("CombatDamageService.java", "CombatOpponentReferencingEffect"): C,
    ("CombatBlockService.java", "CombatOpponentReferencingEffect"): C,
    ("CombatBlockService.java", "DestroyBlockedCreatureAndSelfEffect"): C,
    ("CombatBlockService.java", "DestroyTargetPermanentThenEffect"): C,
    ("CombatBlockService.java", "SkipNextUntapEffect"): C,
    ("CombatBlockService.java", "DealDamageToTargetCreatureEffect"): C,
    ("CombatBlockService.java", "DestroyCombatOpponentAtEndOfCombatEffect"): C,
    ("CombatBlockService.java", "PutCounterOnCombatOpponentAtEndOfCombatEffect"): C,
    ("CombatBlockService.java", "DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect"): C,
    ("CombatBlockService.java", "GrantKeywordEffect"): C,
    ("CombatBlockService.java", "BoostSelfWhenBlockingKeywordEffect"): C,
    ("CombatBlockService.java", "BoostSelfWhenCombatOpponentMatchesEffect"): C,
    # 17z11c2 and 17z11d (done): the "a creature becomes blocked" watchers, the attacker-and-blocker pair and the attack
    # collectors' declared attackers, band, attacked player, granted controller and rebuilt modal no longer read an
    # effect's class; neither does the planar "whenever you attack" collector's GrantKeywordEffect (was 17z11f's).
    # 17z11e (done): the end step's, the draw step's, the opponent's and chosen player's upkeep's and the emblems' "that
    # player" and the enchanted permanent's controller are recorded whatever the effects are; the emblem's step ability
    # is found by its class (ROUTE).
    ("StepTriggerService.java", "EmblemStepTriggerEffect"): "ROUTE",
    # 17z11e2 (done): the Aura grants are granted abilities the your-upkeep and your-end-step readers read (the entry
    # names the granting permanent), the each-upkeep payment's "that player" the declared-target rule, Call to the
    # Kindred's "you may" records the enchanted creature, the Energy Flux family grants its ability, Admiral Beckett
    # Brass's target filter is a predicate. "Choose one at random" picks its mode as the ability goes on the stack, before
    # its targets (CR 603.3c, 700.2b), as a "choose one" does: the class picks the path (ROUTE).
    ("StepTriggerService.java", "ChooseOneAtRandomEffect"): "ROUTE",
    # 17z11e3 (done): Giant Oyster's draw-step counters and counter removal are delayed triggered abilities its
    # activation creates; no printed draw-step ability is skipped or read by class.
    # 17z11f (done; split into 17z11f, 17z11f2 and 17z11f3): every becomes-the-target entry records the spell or ability
    # that targeted (its triggering spell and player) whatever the effects are, and an entry with no other referent whose
    # effect acts only on a spell is aimed at "that spell or ability" (the counter-unless, counterspelling and
    # triggering-spell reads, the ward identification, Chancellor of the Annex's opening-hand counter); Glyph Keeper's
    # "for the first time each turn" is the trigger's limit.
    # 17z11f2: the "when you cast this spell" collector (cascade, demonstrate, ripple, the triggering-spell marker, a
    # granted cascade's single copy) and Venser's emblem.
    ("TriggerCollectionService.java", "ExileTargetOnControllerSpellCastEffect"): F2,
    ("TriggerCollectionService.java", "CascadeEffect"): F2,
    ("TriggerCollectionService.java", "DemonstrateEffect"): F2,
    ("TriggerCollectionService.java", "RippleEffect"): F2,
    ("TriggerCollectionService.java", "TriggeringSpellReferencingEffect"): F2,
    # 17z11f3: TriggerCollectionService's and the enter service's other bindings, and the combat damage rebuilds.
    ("CombatDamageService.java", "GainLifeEqualToControlledCreatureCombatDamageEffect"): F3,
    ("CombatDamageService.java", "ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect"): F3,
    ("CombatDamageService.java", "ReturnPermanentsOnCombatDamageToPlayerEffect"): F3,
    ("EtbTriggerService.java", "TriggeredAbilityCounterEffect"): F3,
    ("EtbTriggerService.java", "DealDividedDamageEffect"): F3,
    ("EtbTriggerService.java", "PreventDividedDamageEffect"): F3,
    ("EtbTriggerService.java", "DistributeCountersAmongTargetsEffect"): F3,
    ("EtbTriggerService.java", "SacrificeArtifactThenDealDividedDamageEffect"): F3,
    ("TriggerCollectionService.java", "SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect"): F3,
    ("TriggerCollectionService.java", "ReturnPermanentsOnCombatDamageToPlayerEffect"): F3,
    ("TriggerCollectionService.java", "TransformSelfWhenChosenPermanentLeavesEffect"): F3,
    ("TriggerCollectionService.java", "RegisterDelayedVehicleAttackEffect"): F3,
    ("TriggerCollectionService.java", "DealDamageToPlayersEffect"): F3,
    ("TriggerCollectionService.java", "ClashOutcomeConditionalEffect"): F3,
    ("TriggerCollectionService.java", "PutCardToBattlefieldEffect"): F3,
    ("TriggerCollectionService.java", "LoseLifeEffect"): F3,
    ("TriggerCollectionService.java", "DealDamageToEnchantedPermanentControllerEffect"): F3,
    ("TriggerCollectionService.java", "RegisterDelayedReturnDyingCreatureUnderControlEffect"): F3,
    ("TriggerCollectionService.java", "TemporaryGlobalTriggerEffect"): F3,
    # Classified ROUTE: the class picks the path or is a trigger condition.
    ("CombatAttackService.java", "SkipNextUntapEffect"): "ROUTE",
    ("CombatAttackService.java", "ChooseOneAtTriggerTimeEffect"): "ROUTE",
    ("CombatAttackService.java", "ChooseOneEffect"): "ROUTE",
    ("CombatAttackService.java", "AttackCounterMoveEffect"): "ROUTE",
    ("CombatAttackService.java", "GraveyardCardChoosingEffect"): "ROUTE",
    ("CombatAttackService.java", "CastTargetInstantOrSorceryFromGraveyardEffect"): "ROUTE",
    ("CombatAttackService.java", "RegisterDelayedVehicleAttackEffect"): "ROUTE",
    ("CombatTriggerService.java", "ChooseOneAtTriggerTimeEffect"): "ROUTE",
    ("CombatDamageService.java", "TransformGrantingPermanentEffect"): "ROUTE",
    ("CombatDamageService.java", "ConjureDuplicateOfCombatDamageSourceIntoHandEffect"): "ROUTE",
    ("CombatDamageService.java", "RenownEffect"): "ROUTE",
    ("CombatDamageService.java", "LoseGameIfSourceDealtDamageToPlayerThisTurnEffect"): "ROUTE",
    ("CombatDamageService.java", "GraveyardCardChoosingEffect"): "ROUTE",
    ("CombatDamageService.java", "EnchantedPermanentConditionalEffect"): "ROUTE",
    ("CombatDamageService.java", "DealDamageToTargetPlayerOrPlaneswalkerEffect"): "ROUTE",
    ("CombatDamageService.java", "DealDamageToAnyTargetEffect"): "ROUTE",
    ("EtbTriggerService.java", "ChooseOneEffect"): "ROUTE",
    ("StepTriggerService.java", "MayRevealSubtypeFromHandEffect"): "ENGINE",
    ("StepTriggerService.java", "RemoveEggCounterFromExileAndReturnEffect"): "ENGINE",
    ("StepTriggerService.java", "ChooseOneEffect"): "ROUTE",
    ("TriggerCollectionService.java", "EnterBattlefieldOnDiscardEffect"): "ROUTE",
    ("TriggerCollectionService.java", "ChooseOneAtTriggerTimeEffect"): F3,
    ("TriggerCollectionService.java", "ChooseOneEffect"): "ROUTE",
    ("TriggerCollectionService.java", "ReselectTargetAtRandomEffect"): "ROUTE",
    ("TriggerCollectionService.java", "TriggeredModalEffect"): "ROUTE",
    ("TriggerCollectionService.java", "EmblemCreatureDeathTriggerEffect"): "ROUTE",
    ("TriggerCollectionService.java", "OptionalTargetEffect"): "ROUTE",
    ("TriggerCollectionService.java", "ChooseSubtypeForSourceEffect"): "STATIC",
    ("TriggerCollectionService.java", "AttachedCreatureBecomesCopyOfExiledCreatureEffect"): "STATIC",
    ("TriggerCollectionService.java", "DrawCardEffect"): "ROUTE",
}
OPEN_ROWS = {F2, F3}

# The other reads run 17z11 read by hand, all ROUTE or STATIC: a restriction, prevention or replacement, an as-enters
# choice, a mana ability resolved at once, a modal, graveyard or target prompt picked, a skip or trigger condition.
REVIEWED_ROUTE = {
    "CombatAttackService.java": {
        "CanOnlyAttackAloneEffect", "CantAttackOrBlockAloneEffect", "CantAttackOrBlockUnlessCountAlsoDoesEffect",
        "CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect", "CombatCreatureLimitEffect",
        "CreaturesWithCounterAttackTogetherEffect", "MatchingAttackerRestrictionEffect",
        "MustAttackIfAnotherCreatureAttacksEffect", "MustAttackPlayerEffect", "OpponentCreaturesAttackTogetherEffect",
        "OpponentsMustAttackRequirementEffect", "OtherCreaturesMustAttackIfSourceAttacksEffect",
    },
    "CombatBlockService.java": {
        "BlockerDeclarationControlEffect", "BushidoEffect", "CantAttackOrBlockAloneEffect",
        "CantAttackOrBlockUnlessCountAlsoDoesEffect", "CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect",
        "CantBeBlockedByFewerThanNCreaturesEffect", "EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect",
        "EachOpponentCreatureBlocksThisTurnIfAbleEffect", "GlobalMustBlockEachCombatEffect",
        "GrantAdditionalBlockEffect", "GrantAdditionalBlockPerEquipmentEffect", "GraveyardCardChoosingEffect",
    },
    "CombatDamageService.java": {
        "AssignCombatDamageAsThoughUnblockedEffect", "AssignCombatDamageToDefendingCreatureWhenUnblockedEffect",
        "CombatDamageAssignmentToDefendingPlayerAndCreaturesEffect", "GainLifeEqualToDamageDealtEffect",
        "PreventDamageAndRemovePlusOnePlusOneCountersEffect", "PreventXDamagePerSourceToControllerAndCreaturesEffect",
        "ReplaceCombatDamageWithMillEffect", "ToxicEffect",
    },
    "EtbTriggerService.java": {
        "BattlefieldAndGraveyardCardChoosingEffect", "CastTargetInstantOrSorceryFromGraveyardEffect",
        "ChooseBasicLandTypeOnEnterEffect", "ChooseColorEffect", "ChooseIndependentModesOnEnterEffect",
        "ChooseModeOnEnterEffect", "ChooseOneAtTriggerTimeEffect", "CopySpellEffect",
        "DraftCardFromSpellbookToExileEffect", "DraftProteanWarEngineSpellbookEffect",
        "ExchangeControlOfTargetPermanentsEffect", "ExileCardsFromGraveyardEffect", "ExileGraveyardCardsEffect",
        "ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect", "GrantFlashbackToTargetGraveyardCardEffect",
        "GraveyardCardChoosingEffect", "PutCardFromOpponentGraveyardOntoBattlefieldEffect",
        "PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect",
        "PutTargetCardsFromGraveyardOnTopOfLibraryEffect", "ReturnCardFromGraveyardEffect",
        "ReturnTargetCardsFromGraveyardToBattlefieldEffect", "ReturnTargetCardsFromGraveyardToHandEffect",
        "SacrificePermanentThenEffect", "SacrificeSelfIfEvokedEffect",
        "ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect", "SoulbondChoosePartnerEffect",
        "SubtypeChoiceOnEnterEffect", "TargetedGraveyardAndPlayerEffect",
    },
    "StepTriggerService.java": {
        "AdditionalUpkeepStepEffect", "BecomeCopyOfTargetCreatureEffect", "ChooseCardTypeOnEnterEffect",
        "ChooseOneAtTriggerTimeEffect", "DealDamageIfFewCardsInHandEffect", "DestroyOneOfTargetsAtRandomEffect",
        "DestroyRandomOpponentPermanentWithCounterEffect", "DrawCardEffect",
        "ExchangeControlOfTargetPermanentsEffect", "ExileGraveyardCardsEffect", "MaySkipDrawReplacementEffect",
        "MonarchEndStepTriggeredEffect", "OpponentDrawStepOnlyEffect",
        "PlayerWithMostLifeGainsControlOfSourceCreatureEffect", "PutCounterOnReturnedPermanentsEffect",
        "PutOrRemoveCounterIfBlockedSinceLastUpkeepEffect", "RemoveCounterAndSacrificeSelfOnLastEffect",
        "RemoveScreamCounterFromExiledCardEffect", "ReturnCardFromGraveyardEffect",
        "ReturnTargetCardsFromGraveyardToHandEffect", "SacrificeSelfAndReturnCardsExiledWithSourceEffect",
        "SkipDrawStepEffect", "SoulEchoUpkeepEffect", "SurveilEffect",
        "TargetPlayerGainsControlOfEnchantedPermanentEffect", "TemporaryCopyEffect", "UpkeepPlayerDependentEffect",
    },
    "TriggerCollectionService.java": {
        "AddOneOfEachManaTypeProducedByLandEffect", "ChooseCardNameAtResolutionEffect", "ChooseColorForSourceEffect",
        "ChooseTargetsAtRandomEffect", "CompleteDungeonEffect", "CounterUnlessDiscardsEffect",
        "CounterUnlessPaysEffect", "CounterUnlessSacrificesEffect", "DealDamageToAnyTargetEffect",
        "EmblemArtifactGraveyardReturnTriggerEffect", "ExileTriggeringSpellWithSuspendCountersEffect",
        "GrantActivatedAbilityEffect", "GrantCastTriggerToSpellsEffect",
        "LoseGameIfSourceDealtDamageToPlayerThisTurnEffect", "NextMatchingSpellCostEffect",
        "PhaseInLinkedPermanentEffect", "ReturnTriggeringPermanentToBattlefieldEffect",
    },
}


# The trigger collectors' `getOpponentId` picks ("an opponent" judged as the one opponent of a two-player game): every
# file of service/trigger/ and the step and enter services. Row 17z11e removed the step collector's last, the fallback
# for a missing chosen player; the combat services' defending-player picks are combat's two-player assumption, not a
# trigger's.
PICK_FILES = sorted({*(S / "trigger").glob("*.java"), S / "turn/StepTriggerService.java",
                     S / "battlefield/EtbTriggerService.java"})
PICKS = {}


def heuristic(lines, i):
    window = "\n".join(lines[i:i + 12])
    return "+".join(k for k, r in (("BIND", BIND), ("REBUILD", REBUILD), ("UNWRAP", UNWRAP))
                    if r.search(window)) or "ROUTE"


def main():
    summary = Counter()
    rows = []
    for name in FILES:
        path = S / name
        base = path.name
        lines = path.read_text().split("\n")
        for i, line in enumerate(lines):
            for match in HIT.finditer(line):
                effect = match.group(1) or match.group(2)
                if effect in WRAPPERS:
                    continue
                kind = CLASSIFIED.get((base, effect))
                if kind is None and effect in REVIEWED_ROUTE.get(base, set()):
                    kind = "ROUTE"
                if kind is None:
                    kind = "STATIC" if base in LEGALITY else f"UNCLASSIFIED({heuristic(lines, i)})"
                summary[kind if not kind.startswith("UNCLASSIFIED") else "UNCLASSIFIED"] += 1
                rows.append(f"{name}:{i + 1}: {effect}: {kind}")
    for path in PICK_FILES:
        for i, line in enumerate(path.read_text().split("\n")):
            if "getOpponentId(" in line:
                kind = PICKS.get(path.name, "UNCLASSIFIED")
                rows.append(f"{path.relative_to(S)}:{i + 1}: getOpponentId: {kind}")
                summary[f"getOpponentId {kind}"] += 1
    if "--summary" in sys.argv:
        for kind, count in sorted(summary.items()):
            print(f"{kind}: {count}{' (open row)' if kind.split()[-1] in OPEN_ROWS else ''}")
        print("reads:", sum(v for k, v in summary.items() if not k.startswith("getOpponentId")))
    else:
        print("\n".join(rows))


if __name__ == "__main__":
    main()
