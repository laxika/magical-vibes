#!/usr/bin/env python3
"""Step 14, run 15 of effect-slot-refactor.md: agent-docs passages that showed family 11's trigger-only wrappers as
the authoring form now show the trigger, its filter or door, its game state and the options (exact replacements; each
old text must occur exactly once). Running it again changes nothing.

Usage: python3 scripts/step14-misc-docs.py [--dry-run]
"""
import sys

D = "agent-docs/"
STATE_NOTE = ("A state trigger's game state is its trigger: `Triggers.state(predicate)`, `Triggers.sourceState("
              "PermanentPredicate)`, `Triggers.battlefieldHasAtMost(n, PermanentPredicate)` / `battlefieldHasAtLeast`, "
              "with `.withSource(…)`, `.withBattlefieldAtMost(…)` and `.referencing(…)`; its effects are the ability's. "
              "Don't author `StateTriggerEffect` (the legacy view builds it for the reader, and only a static grant of "
              "a state trigger, Olivia, Crimson Bride, still writes one)")

REPLACEMENTS = [
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "- `ControllerWasMonarchAtTurnStart()` - condition true when the source permanent's controller was the monarch at "
     "the beginning of the current turn; use with `Triggers.opponentBecomesMonarch()` for Knights of the Black Rose.",
     "- `ControllerWasMonarchAtTurnStart()` - condition true when the source permanent's controller was the monarch at "
     "the beginning of the current turn; the intervening \"if\" of `Triggers.opponentBecomesMonarch()` "
     "(`TriggerOptions.DEFAULT.withInterveningIf(new ControllerWasMonarchAtTurnStart())`) for Knights of the Black "
     "Rose."),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "- `TriggeringRoomDoorConditionalEffect(int doorIndex, CardEffect)` — trigger-only wrapper for "
     "`Triggers.doorUnlocked()`; fires when the specified door of the source Room becomes unlocked, including when "
     "that door unlocks as the Room enters. Used by Grand Entryway // Elegant Rotunda (`DSK`).",
     "- `TriggeringRoomDoorConditionalEffect(int doorIndex, CardEffect)` — don't author: \"when you unlock this door\" "
     "is `Triggers.doorUnlocked(door)` (0 for the left door, 1 for the right one); the legacy view builds this "
     "wrapper for the door collector. It fires when that door of the source Room becomes unlocked, including when "
     "the door unlocks as the Room enters (Grand Entryway // Elegant Rotunda, `DSK`)."),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "Pair with the empty-graveyard `StateTriggerEffect` + `ControllerLosesGameEffect`",
     "Pair with the empty-graveyard `Triggers.state(…)` + `ControllerLosesGameEffect`"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "Pair it with `StateTriggerEffect`'s `PermanentPredicate` reference overload when a state trigger needs to "
     "preserve a permanent reference.",
     "Pair it with a state trigger's `.referencing(PermanentPredicate)` when it needs to preserve a permanent "
     "reference (Bomb Squad)."),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "Use `StateTriggerEffect` with a counter predicate and `PlaneswalkEffect` for a separate counter-threshold state "
     "ability on a face-up plane (Aretopolis).",
     "Use `Triggers.sourceState(counter predicate)` with `PlaneswalkEffect` for a separate counter-threshold state "
     "ability on a face-up plane (Aretopolis)."),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "`Triggers.state() StateTriggerEffect.whenBattlefieldHasAtMost(0, opponent creature filter)` + "
     "`SacrificeSelfEffect()` | controller end step + state trigger | Penregon Besieged",
     "`Triggers.battlefieldHasAtMost(0, opponent creature filter)` `SacrificeSelfEffect()` | controller end step + "
     "state trigger | Penregon Besieged"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "`StateTriggerEffect.whenBattlefieldHasAtMost(0, PermanentAllOfPredicate(PermanentControlledBySourceController"
     "Predicate, PermanentIsLandPredicate), SacrificeSelfEffect())` | FLYING + Triggers.upkeep(ActivePlayer.YOU) + "
     "Triggers.state() |",
     "`Triggers.battlefieldHasAtMost(0, PermanentAllOfPredicate(PermanentControlledBySourceControllerPredicate, "
     "PermanentIsLandPredicate))` `SacrificeSelfEffect()` | FLYING + Triggers.upkeep(ActivePlayer.YOU) + state "
     "trigger |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `TriggeringRoomDoorConditionalEffect(1, CreateTokenEffect(\"Spirit\", UnlockedRoomDoorsCount(CONTROLLER), "
     "UnlockedRoomDoorsCount(CONTROLLER), BLUE, [SPIRIT], [FLYING], []))` | `Triggers.doorUnlocked()` |",
     "| `CreateTokenEffect(\"Spirit\", UnlockedRoomDoorsCount(CONTROLLER), UnlockedRoomDoorsCount(CONTROLLER), BLUE, "
     "[SPIRIT], [FLYING], [])` | `Triggers.doorUnlocked(1)` |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `TriggeringRoomDoorConditionalEffect(0, SearchLibraryEffect(CardAllOf(CardSubtypePredicate(ROOM), "
     "CardDoesNotShareNameWithControlledRoomPredicate)))` | `Triggers.doorUnlocked()` |",
     "| `SearchLibraryEffect(CardAllOf(CardSubtypePredicate(ROOM), CardDoesNotShareNameWithControlledRoomPredicate))` "
     "| `Triggers.doorUnlocked(0)` |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `TriggeringRoomDoorConditionalEffect(0, CreateTokenEffect(\"Glimmer\", 1, 1, WHITE, [GLIMMER], [], "
     "[ENCHANTMENT]))` | `Triggers.doorUnlocked()` |",
     "| `CreateTokenEffect(\"Glimmer\", 1, 1, WHITE, [GLIMMER], [], [ENCHANTMENT])` | `Triggers.doorUnlocked(0)` |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `target(TargetFilters.creature(), 0, 2).addTrigger(Triggers.doorUnlocked(), TriggeringRoomDoorConditionalEffect"
     "(1, PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE)))` | `Triggers.doorUnlocked()` |",
     "| `target(TargetFilters.creature(), 0, 2).addTrigger(Triggers.doorUnlocked(1), PutCounterOnTargetPermanentEffect"
     "(PLUS_ONE_PLUS_ONE))` | `Triggers.doorUnlocked(1)` |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `Triggers.creatureYouControlExplores()` with `TriggeringCardConditionalEffect(CardTypePredicate(LAND), "
     "MayEffect(PutCardToBattlefieldEffect(CardTypePredicate(LAND), \"land\", true)))` plus "
     "`TriggeringCardConditionalEffect(CardNotPredicate(CardTypePredicate(LAND)), PutCountersOnSourceEffect(1, 1, 1))` "
     "| trigger | Nicanzil, Current Conductor (LCI 236); the explore trigger collector evaluates the conditional "
     "wrappers against the revealed card after the explore process completes |",
     "| `Triggers.creatureYouControlExplores().withFilter(CardTypePredicate(LAND))` `MayEffect(PutCardToBattlefield"
     "Effect(CardTypePredicate(LAND), \"land\", true))` plus `Triggers.creatureYouControlExplores().withFilter("
     "CardNotPredicate(CardTypePredicate(LAND)))` `PutCountersOnSourceEffect(1, 1, 1)` | trigger | Nicanzil, Current "
     "Conductor (LCI 236); two abilities, each filtered on the revealed card, which the explore trigger collector "
     "checks after the explore process completes |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `Triggers.state() StateTriggerEffect(opponent life <= 10 and source is an enchantment, BecomeCreatureEffect(3, "
     "2, JACKAL))` | state trigger |",
     "| `Triggers.state(opponent life <= 10)` with `TriggerOptions.DEFAULT.withInterveningIf(new "
     "SourceMatchesPermanentPredicate(new PermanentIsEnchantmentPredicate()))` `BecomeCreatureEffect(3, 2, JACKAL)` "
     "| state trigger; the intervening \"if\" is checked as the state triggers it and on resolution (CR 603.4) |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `Triggers.becomesCrewed()` + `OncePerTurnTriggerEffect(BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(4, 3, "
     "{ARTIFACT}, {VEHICLE}, {FLYING}))` | crew trigger |",
     "| `Triggers.becomesCrewedForFirstTimeEachTurn()` + `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(4, 3, "
     "{ARTIFACT}, {VEHICLE}, {FLYING})` | crew trigger |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "plus `StateTriggerEffect` with `DealDamageToEnchantedPermanentControllerEffect(2)` and "
     "`DestroyReferencedPermanentEffect(PermanentReference.ATTACHED)` | STATIC + Triggers.upkeep(ActivePlayer.YOU) + "
     "Triggers.enchantedPermanentBecomesTapped() + Triggers.state() |",
     "plus `Triggers.sourceState(PermanentNotPredicate(PermanentHasCountersPredicate(ORE)))` with "
     "`DealDamageToEnchantedPermanentControllerEffect(2)` and `DestroyReferencedPermanentEffect(PermanentReference."
     "ATTACHED)` | STATIC + Triggers.upkeep(ActivePlayer.YOU) + Triggers.enchantedPermanentBecomesTapped() + state "
     "trigger |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "`addTrigger(Triggers.state(), StateTriggerEffect.whenBattlefieldHasAtLeast(1, PermanentAllOfPredicate("
     "PermanentNameInPredicate(ARABIAN_NIGHTS_CARD_NAMES), PermanentNotPredicate(PermanentIsTokenPredicate()), "
     "PermanentNotPredicate(PermanentIsSourcePermanentPredicate())), List.of(new SacrificeEachMatchingPermanentEffect("
     "sameFilter)), \"...\"))`",
     "`addTrigger(Triggers.battlefieldHasAtLeast(1, PermanentAllOfPredicate(PermanentNameInPredicate("
     "ARABIAN_NIGHTS_CARD_NAMES), PermanentNotPredicate(PermanentIsTokenPredicate()), PermanentNotPredicate("
     "PermanentIsSourcePermanentPredicate()))), new SacrificeEachMatchingPermanentEffect(sameFilter))`"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `Triggers.state() StateTriggerEffect(PermanentHasKeywordPredicate(FLYING), SacrificeSelfEffect)` + ",
     "| `Triggers.sourceState(PermanentHasKeywordPredicate(FLYING)) SacrificeSelfEffect` + "),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "The state trigger uses the layer-aware `PermanentPredicate` overload so granted flying (Flight, pumps) fires it.",
     "The state trigger's `sourceState` is checked through the layer-aware evaluator, so granted flying (Flight, "
     "pumps) fires it."),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "+ `Triggers.state() StateTriggerEffect(!condition, SacrificeSelfEffect)` | enchantment | Call to Arms (ICE).",
     "+ `Triggers.state(a color is chosen).withSource(PermanentNotPredicate(PermanentChosenColorStrictlyMostCommon"
     "Predicate())) SacrificeSelfEffect` | enchantment | Call to Arms (ICE)."),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "Pair with `Triggers.state() StateTriggerEffect(PermanentPowerAtLeastPredicate(7), SacrificeSelfEffect)` for "
     "\"When this creature's power is 7 or greater, sacrifice it\" |",
     "Pair with `Triggers.sourceState(PermanentPowerAtLeastPredicate(7)) SacrificeSelfEffect` for \"When this "
     "creature's power is 7 or greater, sacrifice it\" |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `Triggers.youDiscover()` + `OncePerTurnTriggerEffect(new DiscoverEffect(new EventValue()))` | "
     "Triggers.youDiscover() |",
     "| `Triggers.youDiscover()` + `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` + `new "
     "DiscoverEffect(new EventValue())` | Triggers.youDiscover() |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "+ `Triggers.state() StateTriggerEffect(PermanentNotPredicate(PermanentHasCountersPredicate(PLUS_ONE_PLUS_ONE)), "
     "SacrificeSelfEffect)` |",
     "+ `Triggers.sourceState(PermanentNotPredicate(PermanentHasCountersPredicate(PLUS_ONE_PLUS_ONE))) "
     "SacrificeSelfEffect` |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `TriggeringRoomDoorConditionalEffect(0, SequenceEffect.of(ManifestDreadEffect.forController(), "
     "PutCountersOnChosenPermanentEffect(PLUS_ONE_PLUS_ONE, 2), PutCountersOnChosenPermanentEffect(TRAMPLE, 1)))` | "
     "`Triggers.doorUnlocked()` |",
     "| `SequenceEffect.of(ManifestDreadEffect.forController(), PutCountersOnChosenPermanentEffect(PLUS_ONE_PLUS_ONE, "
     "2), PutCountersOnChosenPermanentEffect(TRAMPLE, 1))` | `Triggers.doorUnlocked(0)` |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "+ `Triggers.state() StateTriggerEffect(predicate, selector, RemoveAllCountersThenDestroyReferencedPermanentAnd"
     "DamageControllerEffect(TRIGGERING, FUSE, 4))` |",
     "+ `Triggers.state(predicate).referencing(selector) RemoveAllCountersThenDestroyReferencedPermanentAndDamage"
     "ControllerEffect(TRIGGERING, FUSE, 4)` |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `Triggers.doorUnlocked() TriggeringRoomDoorConditionalEffect(1, SequenceEffect.of(ConjureDuplicateOfSourceCard"
     "IntoHandEffect(), QueueReflexiveAbilityEffect(GrantKeywordEffect(FIRST_STRIKE, TARGET, creature-you-control-"
     "filter))))` | Triggers.doorUnlocked() |",
     "| `Triggers.doorUnlocked(1) SequenceEffect.of(ConjureDuplicateOfSourceCardIntoHandEffect(), "
     "QueueReflexiveAbilityEffect(GrantKeywordEffect(FIRST_STRIKE, TARGET, creature-you-control-filter)))` | "
     "Triggers.doorUnlocked(1) |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "+ `Triggers.opponentBecomesMonarch() ConditionalEffect(ControllerWasMonarchAtTurnStart, SequenceEffect.of("
     "LoseLifeEffect(2, TRIGGERING_PLAYER), GainLifeEffect(2, CONTROLLER)))` |",
     "+ `Triggers.opponentBecomesMonarch()` with `TriggerOptions.DEFAULT.withInterveningIf(new "
     "ControllerWasMonarchAtTurnStart())` `SequenceEffect.of(LoseLifeEffect(2, TRIGGERING_PLAYER), GainLifeEffect(2, "
     "CONTROLLER))` |"),
    (D + "ORACLE_TEXT_EFFECT_MAP.md",
     "| `Triggers.youSetSchemeInMotion() SchemeSetInMotionTriggerEffect(List.of(new RegisterNextSchemeSetInMotion"
     "ReplacementEffect()))` |",
     "| `Triggers.youSetSchemeInMotion() RegisterNextSchemeSetInMotionReplacementEffect()` (a scheme filter is "
     "`Triggers.youSetSchemeInMotion().withFilter(CardPredicate)`, My Laughter Echoes) |"),
    (D + "EFFECTS_INDEX.md",
     "Use `StateTriggerEffect` with a counter predicate and `PlaneswalkEffect` for a separate planar counter-threshold "
     "state trigger (Aretopolis).",
     "Use `Triggers.sourceState(counter predicate)` with `PlaneswalkEffect` for a separate planar counter-threshold "
     "state trigger (Aretopolis)."),
    (D + "EFFECTS_INDEX.md",
     "| `TriggeringRoomDoorConditionalEffect` | `(int doorIndex, CardEffect wrapped)` | Fires the wrapped effect when "
     "the specified door of the source Room becomes unlocked, including the door chosen as the Room enters. Target "
     "bindings are preserved through the wrapper. Used by Grand Entryway // Elegant Rotunda (`DSK`) |",
     "| `TriggeringRoomDoorConditionalEffect` | `(int doorIndex, CardEffect wrapped)` | Don't author: \"when you "
     "unlock this door\" is `Triggers.doorUnlocked(door)`, whose legacy view is this wrapper. It fires the wrapped "
     "effect when that door of the source Room becomes unlocked, including the door chosen as the Room enters; "
     "target bindings are read through it (Grand Entryway // Elegant Rotunda, `DSK`) |"),
    (D + "EFFECTS_INDEX.md",
     "| `StateTriggerEffect` | `(StateTriggerPredicate, List<CardEffect>, String)` or `(PermanentPredicate "
     "sourcePredicate, List<CardEffect>, String)` | state-triggered ability (MTG rule 603.8). When the condition is "
     "met after SBAs, pushes effects onto the stack as a triggered ability. Won't retrigger while the ability is on "
     "the stack. Place in `Triggers.state()` slot. The `StateTriggerPredicate` form is a free-form lambda over the "
     "whole game state (Phylactery Lich, Gorilla Pack, Call to Arms). The `PermanentPredicate` form states a "
     "condition about the source permanent only and is evaluated through `PredicateEvaluationService`, so it is CR "
     "613 layer-aware",
     "| `StateTriggerEffect` | `(StateTriggerPredicate, List<CardEffect>, String)` or `(PermanentPredicate "
     "sourcePredicate, List<CardEffect>, String)` | " + STATE_NOTE + ". State-triggered ability (CR 603.8): when the "
     "condition is met after SBAs, its effects go on the stack as a triggered ability, which won't retrigger while "
     "it is on the stack. `Triggers.state(predicate)` is a free-form lambda over the whole game state (Phylactery "
     "Lich, Gorilla Pack, Call to Arms). `Triggers.sourceState(PermanentPredicate)` states a condition about the "
     "source permanent only and is evaluated through `PredicateEvaluationService`, so it is CR 613 layer-aware"),
    (D + "EFFECTS_INDEX.md",
     "| `StateTriggerEffect` reference overload | `(StateTriggerPredicate, PermanentPredicate, List<CardEffect>, "
     "String)` | state-trigger form that snapshots",
     "| `StateTrigger.referencing` | `Triggers.state(predicate).referencing(PermanentPredicate)` | state-trigger form "
     "that snapshots"),
    (D + "EFFECTS_INDEX.md",
     "| `StateTriggerEffect.whenBattlefieldHasAtMost` | `(int maximumMatches, PermanentPredicate predicate, "
     "List<CardEffect> effects, String description)` |",
     "| `Triggers.battlefieldHasAtMost` | `(int maximumMatches, PermanentPredicate predicate)`, the ability's effects "
     "after it |"),
    (D + "EFFECTS_INDEX.md",
     "| `StateTriggerEffect.whenBattlefieldHasAtLeast` | `(int minimumMatches, PermanentPredicate predicate, "
     "List<CardEffect> effects, String description)` |",
     "| `Triggers.battlefieldHasAtLeast` | `(int minimumMatches, PermanentPredicate predicate)`, the ability's effects "
     "after it |"),
    (D + "EFFECTS_INDEX.md",
     "For a separate state-triggered planeswalk, use `StateTriggerEffect` with a counter predicate and "
     "`PlaneswalkEffect`. |",
     "For a separate state-triggered planeswalk, use `Triggers.sourceState(counter predicate)` with "
     "`PlaneswalkEffect`. |"),
    (D + "CARD_PATTERN_INDEX.md",
     "+ `StateTriggerEffect.whenBattlefieldHasAtMost(0, opponent creature filter)` |",
     "+ `Triggers.battlefieldHasAtMost(0, opponent creature filter)` |"),
    (D + "CARD_PATTERN_INDEX.md",
     "| `p/PlotsThatSpanCenturies.java` + `SchemeSetInMotionTriggerEffect(List.of(new RegisterNextSchemeSetInMotion"
     "ReplacementEffect()))` |",
     "| `p/PlotsThatSpanCenturies.java` + `Triggers.youSetSchemeInMotion()` `RegisterNextSchemeSetInMotion"
     "ReplacementEffect()` |"),
    (D + "ACTIVATED_ABILITY_GUIDE.md",
     "Supports `TriggeringCardConditionalEffect` to inspect the revealed card,",
     "`.withFilter(CardPredicate)` inspects the revealed card (one filter per ability),"),
    (D + "ACTIVATED_ABILITY_GUIDE.md",
     "the last-counter payoff is a separate `StateTriggerEffect`)",
     "the last-counter payoff is a separate `Triggers.sourceState(…)` state trigger)"),
    (D + "ACTIVATED_ABILITY_GUIDE.md",
     "| `Triggers.state()` | State-triggered ability (rule 603.8). Fires when predicate is true, won't retrigger while "
     "on stack |",
     "| `Triggers.state(predicate)`, `sourceState`, `battlefieldHasAtMost`, `battlefieldHasAtLeast` | State-triggered "
     "ability (CR 603.8): the trigger is its game state. Fires when it is true, won't retrigger while on the stack; "
     "an intervening \"if\" (`withInterveningIf`) is checked as it triggers and on resolution |"),
    (D + "TRIGGER_SLOT_TARGETING.md",
     "`Triggers.state()`, `Triggers.beginningOfCombat(ActivePlayer.YOU)`,",
     "the state triggers (`Triggers.state(…)`), `Triggers.beginningOfCombat(ActivePlayer.YOU)`,"),
    (D + "CARD_PATTERNS_PERMANENTS_STATIC.md",
     "+ `Triggers.doorUnlocked()` `SequenceEffect.of(ConjureDuplicateOfSourceCardIntoHandEffect(),",
     "+ `Triggers.doorUnlocked(1)` `SequenceEffect.of(ConjureDuplicateOfSourceCardIntoHandEffect(),"),
    (D + "CARD_PATTERNS_PERMANENTS_STATIC.md",
     "+ Triggers.state() sacrifice when condition fails (single-opponent model) |",
     "+ `Triggers.state(a color is chosen).withSource(not strictly most common)` sacrifice when condition fails "
     "(single-opponent model) |"),
    (D + "CARD_PATTERNS_PERMANENTS_STATIC.md",
     "+ Triggers.state() `PermanentHasAtLeastCountersPredicate(DEPLETION, 4)` / `SacrificeSelfEffect`",
     "+ `Triggers.sourceState(PermanentHasAtLeastCountersPredicate(DEPLETION, 4))` / `SacrificeSelfEffect`"),
    (D + "CARD_PATTERNS_PERMANENTS_STATIC.md",
     "+ Triggers.state() `StateTriggerEffect(PermanentNotPredicate(PermanentHasCountersPredicate(ORE)), "
     "[DealDamageToEnchantedPermanentControllerEffect(2), DestroyReferencedPermanentEffect(ATTACHED)])`",
     "+ `Triggers.sourceState(PermanentNotPredicate(PermanentHasCountersPredicate(ORE)))` "
     "`DealDamageToEnchantedPermanentControllerEffect(2)`, `DestroyReferencedPermanentEffect(ATTACHED)`"),
    (D + "EFFECTS_INDEX.md",
     "Tap, life and final-chapter triggers (Interface Ace, Nanoform Sentinel, Moonstone Harbinger, Tom Bombadil) and "
     "creature-death triggers use",
     "Tap, life, final-chapter, crime, discover and Equipment-attach triggers (Interface Ace, Nanoform Sentinel, "
     "Moonstone Harbinger, Tom Bombadil, Magda the Hoardmaster, Curator of Sun's Creation, Inchblade Companion) and "
     "creature-death triggers use"),
    (D + "EFFECTS_INDEX.md",
     "\"whenever this creature attacks for the first time each turn\" (Aurelia, the Warleader), gated by",
     "\"whenever this creature attacks for the first time each turn\" (Aurelia, the Warleader); \"becomes crewed / "
     "saddled for the first time each turn\" is `Triggers.becomesCrewedForFirstTimeEachTurn()` / "
     "`becomesSaddledForFirstTimeEachTurn()` (Mindlink Mech, Stubborn Burrowfiend), whose legacy view is this "
     "wrapper, unkeyed. The attack one is gated by"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "(tap, life and final-chapter triggers (Interface Ace, Nanoform Sentinel, Sharae of Numbing Depths, Moonstone "
     "Harbinger, MACH-1, Tom Bombadil) and creature-death triggers",
     "(tap, life, final-chapter, crime, discover and Equipment-attach triggers (Interface Ace, Nanoform Sentinel, "
     "Sharae of Numbing Depths, Moonstone Harbinger, MACH-1, Tom Bombadil, Magda the Hoardmaster, Curator of Sun's "
     "Creation, Inchblade Companion) and creature-death triggers"),
    (D + "EFFECTS_QUICK_REFERENCE.md",
     "(tap and final-chapter triggers (Interface Ace, Nanoform Sentinel, Tom Bombadil) and creature-death triggers",
     "(tap, final-chapter, crime, discover and Equipment-attach triggers (Interface Ace, Nanoform Sentinel, Tom "
     "Bombadil, Magda the Hoardmaster, Curator of Sun's Creation, Inchblade Companion; \"becomes crewed / saddled for "
     "the first time each turn\" is `Triggers.becomesCrewedForFirstTimeEachTurn()` / "
     "`becomesSaddledForFirstTimeEachTurn()`) and creature-death triggers"),
]


def main():
    dry = "--dry-run" in sys.argv
    texts = {}
    changed = 0
    for path, old, new in REPLACEMENTS:
        src = texts.setdefault(path, open(path, encoding="utf-8").read())
        if src.count(new) == 1 and src.count(old) == 0:
            continue
        if src.count(old) != 1:
            raise SystemExit("%s: the old text occurs %d times: %s" % (path, src.count(old), old[:90]))
        texts[path] = src.replace(old, new)
        changed += 1
    if not dry:
        for path, src in texts.items():
            open(path, "w", encoding="utf-8").write(src)
    print("%d passages" % changed)


if __name__ == "__main__":
    main()
