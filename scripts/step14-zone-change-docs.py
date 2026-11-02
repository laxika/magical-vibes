#!/usr/bin/env python3
"""Step 14, run 3a of effect-slot-refactor.md: rewrites the agent-docs rows that describe step 12a's zone-change and
permanent-change abilities (other than "whenever [an object] enters", run 3b) with trigger-only wrappers, the way
scripts/step14-zone-change-options.py rewrote the cards: a condition on the moved object becomes the trigger's filter,
a controller wrapper the trigger's relation, a limit or an intervening "if" a TriggerOptions. Rows of cards that keep
their wrapper (Urza's Miter, Crawling Sensation, Trail of Mystery, Disa the Restless, Corruption of Towashi, Oasis of
Renewal) and of other trigger families are left alone. Each replacement must match exactly once; one whose new text is
already present is skipped, so the script can be run again.

Usage: python3 scripts/step14-zone-change-docs.py
"""
import sys

DOCS = "agent-docs/"

REPLACEMENTS = {
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("Wrap in `TriggeringCardConditionalEffect` to filter the sacrificed permanent (e.g. `CardSubtypePredicate(CLUE)`). "
         "A source card sacrificed in the same event does not trigger. Scanned in "
         "`TriggerCollectionService.checkGraveyardAllyPermanentSacrificedTriggers`. Used by Curious Cadaver",
         "Narrow it with the trigger's filter to restrict the sacrificed permanent (`Triggers.sacrificed("
         "PermanentHasSubtypePredicate(CLUE), ObjectRelation.YOU_CONTROL)`), judged as it last existed. A source card "
         "sacrificed in the same event does not trigger. Used by Curious Cadaver"),
        ("Used by Scrapheap with `TriggeringCardConditionalEffect(CardAnyOfPredicate(ARTIFACT, ENCHANTMENT), "
         "GainLifeEffect(1))`.",
         "Narrow it with the trigger's filter: Scrapheap is `Triggers.dies(PermanentAnyOfPredicate(List.of("
         "PermanentIsArtifactPredicate(), PermanentIsEnchantmentPredicate())), ObjectRelation.YOU_OWN)` + "
         "`GainLifeEffect(1)`."),
        ("wrap in `TriggeringPermanentConditionalEffect` to filter the leaving permanent from the source's point of view "
         "(My Champion Stands Supreme: `PermanentIsCommanderPredicate(true)`, your commander under any control)",
         "narrow it with the trigger's filter, judged from the source's point of view (My Champion Stands Supreme: "
         "`Triggers.leavesBattlefield(PermanentIsCommanderPredicate(true), ObjectRelation.ANOTHER)`, your commander "
         "under any control)"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("`ConditionalTriggeringPermanentEffect(ControllerTurn, nontoken-and-not-source, "
         "PutCounterOnTargetPermanentEffect.upToOneTarget(SourcePower))`",
         "`Triggers.sacrificed(nontoken-and-not-source, ObjectRelation.ANOTHER_YOU_CONTROL).onlyDuringYourTurn()` + "
         "`PutCounterOnTargetPermanentEffect.upToOneTarget(SourcePower)`"),
        ("`e/EffluenceDevourer.java` + `Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL)` + "
         "`TriggeringPermanentConditionalEffect` + `PerpetuallyGrantGraveyardAbilityToSourceCardEffect`",
         "`e/EffluenceDevourer.java` + `Triggers.sacrificed(PermanentIsCreaturePredicate(), "
         "ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL)` + `PerpetuallyGrantGraveyardAbilityToSourceCardEffect`"),
        ("`v/VolatileRift.java` + `Triggers.putIntoHandFrom(Zone.LIBRARY, null, ObjectRelation.YOU_OWN)` + "
         "`TriggeringCardConditionalEffect(CardTypePredicate(CREATURE), PerpetuallyBoostTriggeringCardEffect("
         "ColorsAmongControlledPermanents, Fixed(0)))`",
         "`v/VolatileRift.java` + `Triggers.putIntoHandFrom(Zone.LIBRARY, PermanentIsCreaturePredicate(), "
         "ObjectRelation.YOU_OWN)` + `PerpetuallyBoostTriggeringCardEffect(ColorsAmongControlledPermanents, Fixed(0))`"),
    ],
    "CARD_PATTERNS_CREATURES_ETB.md": [
        ("`Triggers.putIntoGraveyardFromAnywhere(Triggers.PERMANENT_CARD, ObjectRelation.ANOTHER_YOU_OWN)` "
         "`ConditionalEffect(SourceCounterThreshold(1, MINUS_ONE_MINUS_ONE), RemoveCounterFromSourceEffect("
         "MINUS_ONE_MINUS_ONE, 1))`",
         "`Triggers.putIntoGraveyardFromAnywhere(Triggers.PERMANENT_CARD, ObjectRelation.ANOTHER_YOU_OWN)` with "
         "`TriggerOptions.ONE_OR_MORE.withInterveningIf(SourceCounterThreshold(1, MINUS_ONE_MINUS_ONE))` + "
         "`RemoveCounterFromSourceEffect(MINUS_ONE_MINUS_ONE, 1)`"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.ANY).onlyDuringYourTurn() "
         "`OncePerTurnTriggerEffect(PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))`",
         "Triggers.exiledFrom(Set.of(), Triggers.NONTOKEN, ObjectRelation.ANY).onlyDuringYourTurn() with "
         "`TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN)` + "
         "`PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)`"),
        ("`c/CuriousCadaver.java` | `Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL)` + "
         "`TriggeringCardConditionalEffect(CardSubtypePredicate(CLUE), ReturnSourceCardFromGraveyardToOwnerHandEffect())`",
         "`c/CuriousCadaver.java` | `Triggers.sacrificed(PermanentHasSubtypePredicate(CLUE), "
         "ObjectRelation.YOU_CONTROL)` (functioning from the graveyard) + "
         "`ReturnSourceCardFromGraveyardToOwnerHandEffect()`"),
        ("`c/CultOfTheWaxingMoon.java` | `Triggers.transforms(null, ObjectRelation.YOU_CONTROL)` + "
         "`TriggeringCardConditionalEffect(CardAllOfPredicate(CREATURE, CardNotPredicate(CardSubtypePredicate(HUMAN))), "
         "CreateTokenEffect(Wolf, 2/2, GREEN))`; the transform collector evaluates the destination face",
         "`c/CultOfTheWaxingMoon.java` | `Triggers.transforms(PermanentAllOfPredicate(CREATURE, "
         "PermanentNotPredicate(PermanentHasSubtypePredicate(HUMAN))), ObjectRelation.YOU_CONTROL)` + "
         "`CreateTokenEffect(Wolf, 2/2, GREEN)`; the trigger's filter is judged on the face it transformed into"),
        ("Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) TriggeringPermanentConditionalEffect("
         "PermanentHasSubtypePredicate(TREASURE), BoostSelfEffect(1,0))",
         "Triggers.sacrificed(PermanentHasSubtypePredicate(TREASURE), ObjectRelation.YOU_CONTROL) BoostSelfEffect(1,0)"),
        ("| target opponent + Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY) "
         "TriggeringArtifactControllerConditionalEffect(LoseLifeEffect(SourcePower, TARGET_PLAYER)) — controller check "
         "uses the artifact's controller at trigger time",
         "| target opponent + Triggers.dies(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL) "
         "LoseLifeEffect(SourcePower, TARGET_PLAYER) — \"you control\" is the artifact's last-known controller"),
    ],
    "CARD_PATTERNS_LANDS_SPELLS.md": [
        ("plus wrap the same effect in `TriggeringPermanentConditionalEffect(PermanentHasSubtypePredicate(DESERT), ...)` "
         "on `Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL)`.",
         "plus the same effect on `Triggers.sacrificed(PermanentHasSubtypePredicate(DESERT), "
         "ObjectRelation.YOU_CONTROL)`."),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("`s/Scrapheap.java` | `Triggers.dies(null, ObjectRelation.YOU_OWN)` with `TriggeringCardConditionalEffect("
         "CardAnyOfPredicate(ARTIFACT, ENCHANTMENT), GainLifeEffect(1))`",
         "`s/Scrapheap.java` | `Triggers.dies(PermanentAnyOfPredicate(List.of(PermanentIsArtifactPredicate(), "
         "PermanentIsEnchantmentPredicate())), ObjectRelation.YOU_OWN)` + `GainLifeEffect(1)`"),
        ("Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) `TriggeringPermanentConditionalEffect("
         "PermanentIsArtifactPredicate, MayPayManaEffect(\"{2}\", RegisterDelayedTokenCopyEffect()))`",
         "one ability of two trigger conditions, Triggers.sacrificed(PermanentIsArtifactPredicate(), "
         "ObjectRelation.ANOTHER_YOU_CONTROL) and Triggers.sacrificed(THIS), with "
         "`MayPayManaEffect(\"{2}\", RegisterDelayedTokenCopyEffect())`"),
        ("plus sacrifice-only Triggers.dies(THIS) copy for the source itself;", "for the source itself too;"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED) EnchantedPermanentLeavesConditionalEffect("
         "CardIsHistoricPredicate, DrawCardEffect(2))",
         "Triggers.leavesBattlefield(PermanentIsHistoricPredicate(), ObjectRelation.ENCHANTED) DrawCardEffect(2)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `ConditionalTriggeringPermanentEffect` | `(Condition condition, PermanentPredicate predicate, CardEffect "
         "wrapped)` | trigger-only wrapper for permanent-sacrifice events",
         "| ~~`ConditionalTriggeringPermanentEffect`~~ (removed) | `(Condition condition, PermanentPredicate predicate, "
         "CardEffect wrapped)` | Use the trigger's filter and `onlyDuringYourTurn()` or `TriggerOptions.withInterveningIf` "
         "instead (Szarel: `Triggers.sacrificed(filter, ANOTHER_YOU_CONTROL).onlyDuringYourTurn()`). Was a "
         "trigger-only wrapper for permanent-sacrifice events"),
        ("Currently supports Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) in MiscTriggerCollectorService "
         "(evaluating last-known sacrificed-card data as a permanent), Triggers.creatureYouControlAttacks()",
         "Not for zone changes: a restriction on the object that moved is the trigger's filter "
         "(`Triggers.sacrificed(PermanentHasSubtypePredicate(TREASURE), ObjectRelation.YOU_CONTROL)`). Currently "
         "supports Triggers.creatureYouControlAttacks()"),
        ("Used by Captain Lannery Storm with `PermanentHasSubtypePredicate(TREASURE)`, Catacomb Dragon",
         "Used by Catacomb Dragon"),
        ("| `EnchantedPermanentLeavesConditionalEffect` | `(CardPredicate permanentFilter, List<CardEffect> "
         "resolvedEffects)` |",
         "| ~~`EnchantedPermanentLeavesConditionalEffect`~~ (removed) | | Use "
         "`Triggers.leavesBattlefield(filter, ObjectRelation.ENCHANTED)` with the effects instead (Curator's Ward: "
         "`PermanentIsHistoricPredicate()`, `DrawCardEffect(2)`). Former entry: `(CardPredicate permanentFilter, "
         "List<CardEffect> resolvedEffects)` |"),
        ("Use inside `TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(ANY), MayEffect(...))` for Yuna, "
         "Grand Summoner's optional trigger;",
         "Use inside `MayEffect(...)` on `Triggers.dies(PermanentHasCountersPredicate(ANY), ObjectRelation.ANY)` "
         "for Yuna, Grand Summoner's optional trigger;"),
        ("| `TriggeringArtifactControllerConditionalEffect` | `(CardEffect wrapped[, boolean onlyIfNotSacrificed[, "
         "boolean onlyIfNontoken]])` | Trigger-only wrapper",
         "| `TriggeringArtifactControllerConditionalEffect` | `(CardEffect wrapped[, boolean onlyIfNotSacrificed[, "
         "boolean onlyIfNontoken]])` | Only for \"if it wasn't sacrificed\" (Urza's Miter): \"an artifact you "
         "control\" is `Triggers.dies(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL)` (`ANOTHER_YOU_CONTROL` for "
         "\"another\", `PermanentAllOfPredicate(ARTIFACT, NONTOKEN)` for \"a nontoken artifact\"). Trigger-only wrapper"),
        ("| `TriggeringPermanentControllerConditionalEffect` | `(CardEffect wrapped)` / `.another(CardEffect wrapped)` | "
         "Trigger-only wrapper for `Triggers.dies(null, ObjectRelation.ANY)`:",
         "| `TriggeringPermanentControllerConditionalEffect` | `(CardEffect wrapped)` / `.another(CardEffect wrapped)` | "
         "Not for zone changes: \"a permanent you control is put into a graveyard\" is `Triggers.dies(null, "
         "ObjectRelation.YOU_CONTROL)` (Aetherworks Marvel), \"another\" `ANOTHER_YOU_CONTROL` (Vat of Rebirth). "
         "Trigger-only wrapper for `Triggers.dies(null, ObjectRelation.ANY)`:"),
        ("`TriggeringCardConditionalEffect` evaluates the destination face; Cult of the Waxing Moon gates on creature + "
         "not Human and creates a Wolf token",
         "The trigger's filter is judged on the face it transformed into; Cult of the Waxing Moon filters on creature + "
         "not Human (`Triggers.transforms(PermanentAllOfPredicate(...), ObjectRelation.YOU_CONTROL)`) and creates a "
         "Wolf token"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ConditionalTriggeringPermanentEffect(Condition, PermanentPredicate, CardEffect)` — sacrifice-style permanent "
         "trigger wrapper",
         "- ~~`ConditionalTriggeringPermanentEffect`~~ (removed: use the trigger's filter and `onlyDuringYourTurn()` or "
         "an intervening-if `TriggerOptions`) — was a sacrifice-style permanent trigger wrapper"),
        ("When wrapped in `TriggeringPermanentConditionalEffect` for `Triggers.leavesBattlefield(null, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)`, the trigger collector snapshots the leaving permanent before queuing "
         "target selection (Resourceful Defense)",
         "On `Triggers.leavesBattlefield(PermanentHasCountersPredicate(ANY), ObjectRelation.ANOTHER_YOU_CONTROL)` the "
         "trigger collector snapshots the leaving permanent before queuing target selection (Resourceful Defense, one "
         "ability with that and a `THIS` condition)"),
        ("- `TriggeringPermanentConditionalEffect(PermanentPredicate, CardEffect)` — triggering permanent matches "
         "predicate (Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL), Triggers.creatureYouControlAttacks(), "
         "`Triggers.blocks()`",
         "- `TriggeringPermanentConditionalEffect(PermanentPredicate, CardEffect)` — triggering permanent matches "
         "predicate (Triggers.creatureYouControlAttacks(), `Triggers.blocks()`"),
        ("- `TriggeringPermanentConditionalEffect(PermanentPredicate, CardEffect)` — triggering permanent matches "
         "predicate (Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL), Triggers.creatureYouControlAttacks(), and",
         "- `TriggeringPermanentConditionalEffect(PermanentPredicate, CardEffect)` — triggering permanent matches "
         "predicate (Triggers.creatureYouControlAttacks(), and"),
        ("- `TriggeringArtifactControllerConditionalEffect(CardEffect[, boolean onlyIfNotSacrificed[, boolean "
         "onlyIfNontoken]])` — trigger-only wrapper for `Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)`;",
         "- `TriggeringArtifactControllerConditionalEffect(CardEffect[, boolean onlyIfNotSacrificed[, boolean "
         "onlyIfNontoken]])` — only for \"if it wasn't sacrificed\" (Urza's Miter); \"an artifact you control\" is "
         "`Triggers.dies(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL)` and \"another\" `ANOTHER_YOU_CONTROL`. "
         "Trigger-only wrapper for `Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)`;"),
        ("- `TriggeringPermanentControllerConditionalEffect(CardEffect)` — trigger-only wrapper for "
         "`Triggers.dies(null, ObjectRelation.ANY)`;",
         "- `TriggeringPermanentControllerConditionalEffect(CardEffect)` — not for zone changes: \"a permanent you "
         "control\" is `Triggers.dies(null, ObjectRelation.YOU_CONTROL)` (Aetherworks Marvel), \"another\" "
         "`ANOTHER_YOU_CONTROL`. Trigger-only wrapper for `Triggers.dies(null, ObjectRelation.ANY)`;"),
        ("Wrap the effect in `TriggeringCardConditionalEffect` to inspect the transformed face; Cult of the Waxing Moon "
         "uses a non-Human creature predicate plus `CreateTokenEffect`.",
         "Its filter is judged on the transformed face; Cult of the Waxing Moon uses a non-Human creature filter plus "
         "`CreateTokenEffect`."),
        ("wrap in `MayEffect` and `OncePerTurnTriggerEffect` for Screeching Scorchbeast (PIP 49).",
         "wrap in `MayEffect`, with `TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`, for "
         "Screeching Scorchbeast (PIP 49)."),
        ("The effect is normally wrapped in `TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(ANY), "
         "MayEffect(...))`;",
         "The effect is normally wrapped in `MayEffect(...)` on `Triggers.dies(PermanentHasCountersPredicate(ANY), "
         "ObjectRelation.ANY)`;"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) TriggeringPermanentConditionalEffect("
         "PermanentIsCreaturePredicate, PerpetuallyGrantGraveyardAbilityToSourceCardEffect(",
         "`Triggers.sacrificed(PermanentIsCreaturePredicate(), ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL) "
         "PerpetuallyGrantGraveyardAbilityToSourceCardEffect("),
        ("`Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) TriggeringPermanentConditionalEffect("
         "PermanentHasSubtypePredicate(CLUE), DraftCardFromSpellbookEffect(SPELLBOOK))` + `Triggers.sacrificed(THIS) "
         "DraftCardFromSpellbookEffect(SPELLBOOK)` | Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) + "
         "Triggers.sacrificed(THIS) | Slayer's Bounty (YMID 10). The ally-sacrifice trigger handles other Clues;",
         "`DraftCardFromSpellbookEffect(SPELLBOOK)` on one `TriggeredAbility` with two trigger conditions, "
         "`Triggers.sacrificed(PermanentHasSubtypePredicate(CLUE), ObjectRelation.ANOTHER_YOU_CONTROL)` and "
         "`Triggers.sacrificed(THIS)` | Triggers.sacrificed(filter, ObjectRelation.ANOTHER_YOU_CONTROL) + "
         "Triggers.sacrificed(THIS) | Slayer's Bounty (YMID 10). The ally-sacrifice condition handles other Clues;"),
        ("`Triggers.dies(THIS)` + `Triggers.dies(null, ObjectRelation.ANOTHER_YOU_CONTROL)`: "
         "`SacrificePermanentsSharingDyingPermanentTypeEffect()`; wrap the ally marker in "
         "`TriggeringPermanentConditionalEffect(PermanentNotPredicate(PermanentIsLandPredicate()), ...)`",
         "`Triggers.dies(THIS)` + `Triggers.dies(PermanentNotPredicate(PermanentIsLandPredicate()), "
         "ObjectRelation.ANOTHER_YOU_CONTROL)`: `SacrificePermanentsSharingDyingPermanentTypeEffect()`"),
        ("on `Triggers.leavesBattlefield(new PermanentHasCountersPredicate(ANY), THIS)` + "
         "`TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(ANY), "
         "PutCountersOnTargetForEachLeavingSourceCountersEffect(PermanentTruePredicate()))` on "
         "`Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL)`",
         "on one `TriggeredAbility` (`target(...).addTriggeredAbility`) with two trigger conditions, "
         "`Triggers.leavesBattlefield(hadCounters, THIS)` and `Triggers.leavesBattlefield(hadCounters, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)`, `hadCounters = PermanentHasCountersPredicate(ANY)`"),
        ("| `TriggeringPermanentControllerConditionalEffect(EnergyCountersEffect(1))` | `Triggers.dies(null, "
         "ObjectRelation.ANY)` | Aetherworks Marvel (KLD). The controller check is made from the triggering "
         "permanent's controller at the graveyard event,",
         "| `EnergyCountersEffect(1)` | `Triggers.dies(null, ObjectRelation.YOU_CONTROL)` | Aetherworks Marvel (KLD). "
         "\"You control\" is the permanent's last-known controller,"),
        ("| `TriggeringPermanentConditionalEffect(PermanentIsArtifactPredicate(), DealDamageToPlayersEffect(2, "
         "TRIGGERING_PLAYER))` | `Triggers.sacrificed(null, ObjectRelation.OPPONENT_CONTROLS)` |",
         "| `DealDamageToPlayersEffect(2, TRIGGERING_PLAYER)` | `Triggers.sacrificed(PermanentIsArtifactPredicate(), "
         "ObjectRelation.OPPONENT_CONTROLS)` |"),
        ("| `Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL)` + `TriggeringCardConditionalEffect("
         "CardSubtypePredicate(CLUE), ReturnSourceCardFromGraveyardToOwnerHandEffect())` | graveyard trigger | Curious "
         "Cadaver (MKM). The graveyard collector scans the sacrificing player's graveyard and filters the sacrificed "
         "permanent by its last-known subtype",
         "| `Triggers.sacrificed(PermanentHasSubtypePredicate(CLUE), ObjectRelation.YOU_CONTROL)` (from the graveyard) "
         "+ `ReturnSourceCardFromGraveyardToOwnerHandEffect()` | graveyard trigger | Curious Cadaver (MKM). The filter "
         "is judged on the sacrificed permanent as it last existed"),
        ("-> `Triggers.dies(Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` + `TriggeringCardConditionalEffect("
         "CardSubtypePredicate(CURSE), RegisterDelayedReturnCurseAttachedToPlayerEffect(null, controllerId))`",
         "-> `Triggers.dies(PermanentAllOfPredicate(Triggers.NONTOKEN, PermanentHasSubtypePredicate(CURSE)), "
         "ObjectRelation.YOU_OWN)` + `RegisterDelayedReturnCurseAttachedToPlayerEffect(null, controllerId)`"),
        ("| `Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` + "
         "`TriggeringPermanentConditionalEffect(new PermanentIsModifiedPredicate(), new PutCountersOnSourceEffect(1, 1, "
         "1))`, and for this artifact itself as a modified creature `Triggers.leavesBattlefield(new "
         "PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentIsModifiedPredicate())), "
         "ObjectRelation.THIS)` + `new PutCountersOnSourceEffect(1, 1, 1)` |",
         "| `PutCountersOnSourceEffect(1, 1, 1)` on one `TriggeredAbility` with two trigger conditions, "
         "`Triggers.leavesBattlefield(modifiedCreature, THIS)` (this artifact once it is a modified creature) and "
         "`Triggers.leavesBattlefield(modifiedCreature, ObjectRelation.ANOTHER_YOU_CONTROL)`, `modifiedCreature = "
         "PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsModifiedPredicate()))` |"),
        ("`target(PlayerPredicateTargetFilter(OPPONENT)).addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY), "
         "TriggeringArtifactControllerConditionalEffect(LoseLifeEffect(SourcePower, TARGET_PLAYER)))` | trigger | "
         "Marionette Master. The artifact controller is checked when the artifact leaves the battlefield;",
         "`target(PlayerPredicateTargetFilter(OPPONENT)).addTrigger(Triggers.dies(Triggers.ARTIFACT, "
         "ObjectRelation.YOU_CONTROL), LoseLifeEffect(SourcePower, TARGET_PLAYER))` | trigger | Marionette Master. "
         "\"You control\" is the artifact's last-known controller;"),
        ("| `Triggers.putIntoGraveyardFromAnywhere(Triggers.PERMANENT_CARD, ObjectRelation.ANOTHER_YOU_OWN)` | Moonshadow.",
         "| `Triggers.putIntoGraveyardFromAnywhere(Triggers.PERMANENT_CARD, ObjectRelation.ANOTHER_YOU_OWN)` with "
         "`TriggerOptions.ONE_OR_MORE.withInterveningIf(SourceCounterThreshold(1, MINUS_ONE_MINUS_ONE))` and the "
         "effect `RemoveCounterFromSourceEffect(MINUS_ONE_MINUS_ONE, 1)` | Moonshadow."),
        ("| `ConditionalEffect(ControllerTurn, PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, 1))` | "
         "`Triggers.leavesGraveyard(Triggers.ARTIFACT_OR_CREATURE, ObjectRelation.YOU_OWN)` | Thran Vigil.",
         "| `PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, 1)` | `Triggers.leavesGraveyard("
         "Triggers.ARTIFACT_OR_CREATURE, ObjectRelation.YOU_OWN).onlyDuringYourTurn()` | Thran Vigil."),
        ("`Triggers.dies(THIS)` + `Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)` "
         "`TriggeringArtifactControllerConditionalEffect(ReturnCardFromGraveyardEffect.builder()",
         "one `TriggeredAbility` with two trigger conditions, `Triggers.dies(THIS)` and `Triggers.dies("
         "Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL)`, `(ReturnCardFromGraveyardEffect.builder()"),
        ("| `addTrigger(Triggers.transforms(null, ObjectRelation.YOU_CONTROL), new TriggeringCardConditionalEffect("
         "CardAllOfPredicate(CREATURE, CardNotPredicate(CardSubtypePredicate(HUMAN))), new CreateTokenEffect(\"Wolf\", 2, "
         "2, GREEN, List.of(WOLF), Set.of(), Set.of())))` |",
         "| `addTrigger(Triggers.transforms(PermanentAllOfPredicate(List.of(PermanentIsCreaturePredicate(), "
         "PermanentNotPredicate(PermanentHasSubtypePredicate(HUMAN)))), ObjectRelation.YOU_CONTROL), new "
         "CreateTokenEffect(\"Wolf\", 2, 2, GREEN, List.of(WOLF), Set.of(), Set.of()))` |"),
        ("+ `OncePerTurnTriggerEffect(TriggeringArtifactControllerConditionalEffect(TransformSelfEffect(), false, true))` "
         "on Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)",
         "+ `TransformSelfEffect()` on Triggers.dies(PermanentAllOfPredicate(Triggers.ARTIFACT, Triggers.NONTOKEN), "
         "ObjectRelation.YOU_CONTROL) with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`"),
        ("+ `OncePerTurnTriggerEffect(new MayEffect(new CreateTokensForNonlandCardsMilledEffect(zombieMutant), ...))` on "
         "`Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)`",
         "+ `new MayEffect(new CreateTokensForNonlandCardsMilledEffect(zombieMutant), ...)` on "
         "`Triggers.mills(Triggers.NONLAND, ObjectRelation.ANY)` with "
         "`TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`"),
        ("| `TriggeringCardConditionalEffect(new CardSupertypePredicate(CardSupertype.LEGENDARY), new "
         "ReturnTriggeringCardToOwnerHandEffect())` | `Triggers.dies(null, ObjectRelation.ANY)` |",
         "| `new ReturnTriggeringCardToOwnerHandEffect()` | `Triggers.dies(new "
         "PermanentHasSupertypePredicate(CardSupertype.LEGENDARY), ObjectRelation.ANY)` |"),
        ("| `TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(ANY), MayEffect("
         "PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect()))` | `Triggers.dies(null, "
         "ObjectRelation.ANY)` | Yuna, Grand Summoner (FIC 8). The any-permanent collector filters the dying permanent "
         "by its last-known controller,",
         "| `MayEffect(PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect())` | `Triggers.dies("
         "PermanentHasCountersPredicate(ANY), ObjectRelation.ANY)` | Yuna, Grand Summoner (FIC 8). The effect "
         "checks the dying permanent's last-known controller,"),
        ("`Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)` + "
         "`TriggeringPermanentConditionalEffect(PermanentHasCountersPredicate(ANY), PutLeavingCreatureCountersOnSourceEffect())`;",
         "`Triggers.leavesBattlefield(PermanentAllOfPredicate(Triggers.CREATURE, PermanentHasCountersPredicate(ANY)), "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` + `PutLeavingCreatureCountersOnSourceEffect()`;"),
        ("| `TriggeringCardConditionalEffect(CardAnyOfPredicate(ARTIFACT, ENCHANTMENT), GainLifeEffect(1))` | "
         "`Triggers.dies(null, ObjectRelation.YOU_OWN)` | Scrapheap. The slot is graveyard-owner scoped and includes "
         "tokens; the conditional filters the dying permanent's last-known card types.",
         "| `GainLifeEffect(1)` | `Triggers.dies(PermanentAnyOfPredicate(List.of(PermanentIsArtifactPredicate(), "
         "PermanentIsEnchantmentPredicate())), ObjectRelation.YOU_OWN)` | Scrapheap. The slot is graveyard-owner "
         "scoped and includes tokens; the filter reads the dying permanent's last-known card types."),
        ("| `ConditionalEffect(new SourceCounterThreshold(3, CounterType.QUEST), new MayEffect(new LoseLifeEffect(2, "
         "TARGET_PLAYER, true), \"Have that player lose 2 life?\"))` | `Triggers.putIntoGraveyardFromAnywhere("
         "Triggers.NONTOKEN, ObjectRelation.OPPONENT_OWNS)` |",
         "| `new MayEffect(new LoseLifeEffect(2, TARGET_PLAYER, true), \"Have that player lose 2 life?\")` with "
         "`TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(3, CounterType.QUEST))` | "
         "`Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.OPPONENT_OWNS)` |"),
        ("+ `TriggeringPermanentConditionalEffect(PermanentHasSubtypePredicate(CLUE), MayEffect("
         "BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect()))` | `Triggers.attacks()` + `Triggers.sacrificed("
         "null, ObjectRelation.YOU_CONTROL)` |",
         "+ `MayEffect(BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect())` | `Triggers.attacks()` + "
         "`Triggers.sacrificed(PermanentHasSubtypePredicate(CLUE), ObjectRelation.YOU_CONTROL)` |"),
        ("`target(ControlledPermanentPredicateTargetFilter(AnyOf(IsArtifact, IsCreature)), 0, 1).addTrigger("
         "Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY), TriggeringArtifactControllerConditionalEffect("
         "PutCountersOnTargetForEachLeavingSourceCountersEffect(artifactOrCreatureYouControl)))`",
         "`target(ControlledPermanentPredicateTargetFilter(AnyOf(IsArtifact, IsCreature)), 0, 1).addTrigger("
         "Triggers.dies(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL), "
         "PutCountersOnTargetForEachLeavingSourceCountersEffect(artifactOrCreatureYouControl))`"),
        ("| `Triggers.putIntoHandFrom(Zone.LIBRARY, null, ObjectRelation.YOU_OWN)` + `TriggeringCardConditionalEffect("
         "CardSubtypePredicate(KITHKIN), ConjureDuplicateOfCardPutIntoHandFromLibraryEffect())` +",
         "| `Triggers.putIntoHandFrom(Zone.LIBRARY, PermanentHasSubtypePredicate(KITHKIN), ObjectRelation.YOU_OWN)` + "
         "`ConjureDuplicateOfCardPutIntoHandFromLibraryEffect()` +"),
        ("| `TriggeringPermanentConditionalEffect(PermanentAllOfPredicate(HasSubtype(ELEMENTAL), Not(IsToken)), "
         "CreateTokenCopyOfSacrificedPermanentEffect())` | `Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL)` |",
         "| `CreateTokenCopyOfSacrificedPermanentEffect()` | `Triggers.sacrificed(PermanentAllOfPredicate("
         "HasSubtype(ELEMENTAL), Not(IsToken)), ObjectRelation.YOU_CONTROL)` |"),
        ("| `Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN) OncePerTurnTriggerEffect("
         "ConjureRandomCardFromSpellbookToBattlefieldEffect(SPELLBOOK, 2, 2))` |",
         "| `Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)` with "
         "`TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN)` + "
         "`ConjureRandomCardFromSpellbookToBattlefieldEffect(SPELLBOOK, 2, 2)` |"),
        ("| `Triggers.putIntoHandFrom(Zone.LIBRARY, null, ObjectRelation.YOU_OWN) TriggeringCardConditionalEffect("
         "CardTypePredicate(CREATURE), PerpetuallyBoostTriggeringCardEffect(ColorsAmongControlledPermanents, "
         "Fixed(0)))` |",
         "| `Triggers.putIntoHandFrom(Zone.LIBRARY, PermanentIsCreaturePredicate(), ObjectRelation.YOU_OWN) "
         "PerpetuallyBoostTriggeringCardEffect(ColorsAmongControlledPermanents, Fixed(0))` |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("(non-targeting; supports `TriggeringCardConditionalEffect` against the transformed face)",
         "(non-targeting; the trigger's filter is judged on the transformed face)"),
        ("(non-targeting effects use the direct stack path; `TriggeringArtifactControllerConditionalEffect` uses the "
         "spell-target pipeline),",
         "(non-targeting effects use the direct stack path; a declared target, \"an artifact you control\" "
         "(`ObjectRelation.YOU_CONTROL`) included, uses the spell-target pipeline),"),
    ],
}


def main():
    failed = False
    for name, pairs in REPLACEMENTS.items():
        path = DOCS + name
        text = open(path, encoding="utf-8", errors="surrogateescape").read()
        for old, new in pairs:
            if new in text:
                continue
            count = text.count(old)
            if count != 1:
                print("%s: %d matches for %r" % (name, count, old[:90]))
                failed = True
                continue
            text = text.replace(old, new)
        open(path, "w", encoding="utf-8", errors="surrogateescape").write(text)
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
