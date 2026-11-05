#!/usr/bin/env python3
"""Rewrites agent-docs and the skills for the wrapper classes run 16t deleted (effect-slot-refactor.md step 14).

The reference entries of the deleted classes (TriggeringCardConditionalEffect, TriggeringPermanentConditionalEffect,
TriggeringPermanentControllerConditionalEffect, TriggeringArtifactControllerConditionalEffect,
TriggeringAbilityFromNamedLandConditionalEffect, SelfExiledFromBattlefieldEffect, OncePerTurnPerCreatureTriggerEffect,
and OncePerTurnTriggerEffect's keyed, per-grant, mark-on-acceptance and first-time forms) are replaced by entries for
what cards write instead; every other mention is rewritten to the current form of the cards it names. Exact
replacements, skipped once the new text is there, so running it again changes nothing.

Usage: python3 scripts/step14-wrapper-classes-docs.py
"""
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCS = os.path.join(ROOT, "agent-docs")

DELETED = ("TriggeringCardConditionalEffect", "TriggeringPermanentConditionalEffect",
           "TriggeringPermanentControllerConditionalEffect", "TriggeringArtifactControllerConditionalEffect",
           "TriggeringAbilityFromNamedLandConditionalEffect", "SelfExiledFromBattlefieldEffect",
           "OncePerTurnPerCreatureTriggerEffect", "OncePerTurnTriggerEffect")

LIMITS_BULLET = (
    "- Trigger limits are `TriggerOptions`, never a wrapper effect: \"This ability triggers only once each turn\" is "
    "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` (one ability of several trigger conditions "
    "shares it: Dennick, Pious Apparition; a granted ability keeps one limit for each granting permanent: Well "
    "Rested); \"do this only once each turn\" is `TriggerLimit.DO_ONLY_ONCE_EACH_TURN`, used up as the action is "
    "taken (Nykthos Paragon, Spider-Verse); \"for the first time each turn\" in the trigger condition is "
    "`TriggerLimit.FIRST_TIME_EACH_TURN`, counted whether or not the source was there for the earlier events, read by "
    "the token-creation (Akim, the Soaring Wind), land-to-graveyard (Crawling Sensation) and \"whenever you attack\" "
    "(Desert Were-Worm, with `withEventCondition(new AttackingCreaturesTotalPowerAtLeast(12))`) collectors. "
    "`OncePerTurnTriggerEffect(CardEffect)` is left only on Bill Potts, whose one ability is two trigger conditions "
    "with two event-binding effects (step 15); don't author it.")

FILTERS_BULLET = (
    "- Filters on what triggers an ability are the trigger's, never a wrapper effect: `Triggers.creatureDies(filter, "
    "relation)`, `.withFilter(filter)` on combat, damage, tap, target and counter triggers (\"a Rat you control becomes "
    "blocked\", Tattered Ratter; \"an Assassin you control attacks\" on `Triggers.creatureAttacks()` is "
    "`withFilter(PermanentAllOfPredicate(PermanentControlledBySourceControllerPredicate, "
    "PermanentHasSubtypePredicate(ASSASSIN)))`, judged from the ability's controller); \"when this is put into exile "
    "from the battlefield\" is `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.THIS)` (God-Eternal "
    "Oketra; the ability refers to the exile object the card became, CR 400.7); \"if it wasn't sacrificed\" is "
    "`Triggers.diesWithoutBeingSacrificed(filter, relation)` (Urza's Miter); \"deals combat damage to a planeswalker\" "
    "is `Triggers.creatureYouControlDealsCombatDamageToPlaneswalker()` (Zagras); \"becomes the target of an ability of "
    "a land you control named …\" is `TargetTrigger.withSource(PermanentPredicate)` (Skophos Maze-Warden); a boon's "
    "filter is `CreateBoonEffect(uses, effect, trigger, targetFilter, CardPredicate filter)` (Merfolk Tunnel-Guide). "
    "An \"if\" of an effect on the card that triggered the ability (\"if that creature was a Villain\", \"if that "
    "spell's power is 4 or greater\", \"if it's a Samurai card\") is `ConditionalEffect(new "
    "TriggeringCardMatches(CardPredicate), effect)`, checked as it resolves (Venom, Eddie Brock; Eshki, Temur's Roar; "
    "Oathkeeper, Takeno's Daisho).")

INDEX_LIMITS_ROW = (
    "| `OncePerTurnTriggerEffect` | `(CardEffect wrapped)` | Bill Potts only: its \"this ability triggers only once "
    "each turn\" covers a cast trigger and an activation trigger whose effects are two event bindings, so the source is "
    "marked when either fires (step 15 makes it one ability). Don't author it: \"triggers only once each turn\" is "
    "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`, \"do this only once each turn\" "
    "`TriggerLimit.DO_ONLY_ONCE_EACH_TURN`, \"for the first time each turn\" `TriggerLimit.FIRST_TIME_EACH_TURN` |")

INDEX_FILTERS_ROW = (
    "| `TriggeringCardMatches` (condition) | `(CardPredicate predicate)` | `ConditionalEffect(new "
    "TriggeringCardMatches(p), effect)`: an \"if\" of an effect on the card that triggered the ability, checked as it "
    "resolves against the stack entry's triggering card (\"if that creature was a Villain\", Venom, Eddie Brock; \"if "
    "that spell was all colors\", Jenson Carthalion; \"if it's a Samurai card\", Oathkeeper). A filter on what triggers "
    "the ability is the trigger's (`.withFilter(...)`, `Triggers.creatureDies(filter, relation)`, "
    "`Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, THIS)`, `Triggers.diesWithoutBeingSacrificed(...)`, "
    "`TargetTrigger.withSource(...)`) |")

# file -> [(old, new)] exact replacements.
REPLACEMENTS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `OncePerTurnTriggerEffect.markOnAcceptance(NykthosParagonLifeGainEffect())` — Triggers.youGainLife() marker:",
         "- `NykthosParagonLifeGainEffect()` — Triggers.youGainLife() with "
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`:"),
        ("Wrap in `TriggeringPermanentControllerConditionalEffect` and `TriggeringPermanentConditionalEffect` for "
         "\"an Assassin you control\".",
         "\"An Assassin you control\" is the trigger's filter: `Triggers.creatureDealsCombatDamageToOpponent()"
         ".withFilter(PermanentAllOfPredicate(PermanentControlledBySourceControllerPredicate, "
         "PermanentHasSubtypePredicate(ASSASSIN)))` (Etrata, Deadly Fugitive)."),
        ("`Triggers.creatureBlocks()` also evaluates a `TriggeringPermanentConditionalEffect` against the blocker "
         "before queuing the trigger.", "`Triggers.creatureBlocks()` stamps the blocker as the non-targeting target."),
        ("and support `TriggeringPermanentConditionalEffect` filters.", "and judge the trigger's filter."),
        ("`Triggers.leavesBattlefield(THIS)` (including `SelfExiledFromBattlefieldEffect`),",
         "`Triggers.leavesBattlefield(THIS)` and `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, THIS)`,"),
    ],
    "EFFECTS_INDEX.md": [
        ("`OncePerTurnTriggerEffect.firstLandGraveyardEvent(wrapped)` includes land-card arrivals earlier in the turn "
         "before the source entered the battlefield. Use this for a first-event trigger rather than an ability that can "
         "trigger only once.",
         "`TriggerLimit.FIRST_TIME_EACH_TURN` (\"for the first time each turn\") includes events earlier in the turn "
         "before the source entered the battlefield (Crawling Sensation's land cards, Akim's token creation). Use it for "
         "a first-event trigger rather than `TriggerLimit.ONCE_EACH_TURN`, which counts only the source's own triggers."),
        ("For a card-subtype-qualified form that should fire once per matching blocked attacker, wrap the effect in "
         "`TriggeringCardConditionalEffect`; the combat collector evaluates the card predicate globally",
         "For a subtype-qualified form that should fire once per matching blocked attacker, give the trigger a "
         "filter (`Triggers.creatureBecomesBlocked().withFilter(PermanentHasSubtypePredicate(SLIVER))`, Spined "
         "Sliver); the combat collector judges the filter on each blocked attacker"),
        ("`Triggers.creatureDies(EQUIPPED)` wrapped in `TriggeringCardConditionalEffect(CardSubtypePredicate(SAMURAI), "
         "…)` for \"return that card … if it's a Samurai card\"",
         "`Triggers.creatureDies(EQUIPPED)` with `ConditionalEffect(TriggeringCardMatches(CardSubtypePredicate(SAMURAI)), "
         "…)` for \"return that card … if it's a Samurai card\""),
        ("Use `TriggeringPermanentControllerConditionalEffect` plus `TriggeringPermanentConditionalEffect` to restrict "
         "the damage source to a matching creature the controller",
         "Give the trigger the filter `PermanentAllOfPredicate(PermanentControlledBySourceControllerPredicate, …)` to "
         "restrict the damage source to a matching creature the controller"),
        ("Wrap in `MayEffect` (and a toughness/other `TriggeringPermanentConditionalEffect`) for the \"you may exile "
         "it\" clause.", "Wrap in `MayEffect` (with a toughness or other filter on the trigger) for the \"you may "
         "exile it\" clause."),
        ("`Triggers.creatureBlocks()` stamps the blocking creature as a non-targeting permanent target and evaluates "
         "a `TriggeringPermanentConditionalEffect` against it before queuing the trigger.",
         "`Triggers.creatureBlocks()` stamps the blocking creature as a non-targeting permanent target."),
        ("Used by Ashnod's Intervention via `SelfExiledFromBattlefieldEffect` and "
         "`GrantEffectToTargetUntilEndOfTurnEffect(Triggers.leavesBattlefield(THIS), ...)`",
         "Used by Ashnod's Intervention via `GrantEffectToTargetUntilEndOfTurnEffect(new TriggeredAbility("
         "Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, THIS), ...))`"),
        ("Pair with `OncePerTurnTriggerEffect.markOnAcceptance` when acceptance, rather than triggering, consumes the "
         "once-per-turn limit (Spider-Verse)",
         "Pair with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` when acceptance, rather "
         "than triggering, consumes the once-per-turn limit (Spider-Verse)"),
    ],
    "ARCHITECTURE.md": [
        ("A wrapper handler that only checks a condition and then resolves one wrapped effect "
         "(`TriggeringCardConditionalEffectHandler`) implements `ResolvesWrappedEffectFirst`,",
         "A wrapper handler that only checks a condition and then resolves one wrapped effect implements "
         "`ResolvesWrappedEffectFirst`,"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("`TriggeringPermanentConditionalEffect` wrappers are checked against the blocked attacker at trigger creation "
         "and unwrapped when they match (Arrogant Bloodlord).",
         "The trigger's filter is judged on the blocked attacker (`Triggers.blocks().withFilter(...)`, Arrogant "
         "Bloodlord)."),
        ("attacking creature stored as non-targeting `targetId`; `TriggeringPermanentConditionalEffect` filters which "
         "attackers trigger)", "attacking creature stored as non-targeting `targetId`; the trigger's filter decides "
         "which attackers trigger)"),
        ("attack target stored as `attackedTargetId`; `TriggeringPermanentConditionalEffect` filters which attackers "
         "trigger)", "attack target stored as `attackedTargetId`; the trigger's filter decides which attackers "
         "trigger)"),
        ("`TriggeringAbilityFromNamedLandConditionalEffect` can retain its wrapped effect only for an ability from a "
         "named land controlled by the watcher and targeting ano",
         "`TargetTrigger.withSource(...)` restricts it to an ability of a matching permanent (Skophos Maze-Warden: a "
         "land you control named Labyrinth of Skophos) targeting ano"),
        ("collector pre-sets the damage target to the land's controller; `TriggeringCardConditionalEffect`",
         "collector pre-sets the damage target to the land's controller; a `TriggeringCardMatches` \"if\""),
        ("`OncePerTurnTriggerEffect` for “the first time each turn”),",
         "`TriggerLimit.FIRST_TIME_EACH_TURN` for “the first time each turn”),"),
        ("wrap in `TriggeringPermanentConditionalEffect` to filter by the renowned",
         "give the trigger a filter to restrict the renowned"),
        ("use `TriggeringPermanentConditionalEffect` to narrow it to tokens or another permanent predicate.",
         "use the trigger's filter to narrow it to tokens or another permanent predicate."),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("`s/ScourTheScene.java` + `TriggeringPermanentConditionalEffect(PermanentIsArtifactPredicate, "
         "ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect(1, 0))`",
         "`s/ScourTheScene.java` + `Triggers.sacrificed(PermanentIsArtifactPredicate, YOU_CONTROL)` + "
         "`ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect(1, 0)`"),
        ("`d/DivinerOfFates.java` + `DrawDiscardAndConniveEffect` + `OncePerTurnTriggerEffect` + ",
         "`d/DivinerOfFates.java` + `DrawDiscardAndConniveEffect` + `TriggerLimit.ONCE_EACH_TURN` + "),
        ("`ChooseSubtypeOnEnterEffect` + `TriggeringPermanentConditionalEffect(PermanentAllOfPredicate("
         "PermanentHasSourceChosenSubtypePredicate, PermanentNotPredicate(PermanentIsTokenPredicate)), ",
         "`ChooseSubtypeOnEnterEffect` + the trigger's filter `PermanentAllOfPredicate("
         "PermanentHasSourceChosenSubtypePredicate, PermanentNotPredicate(PermanentIsTokenPredicate))` + `"),
        ("`Triggers.youCastSpell()` + `OncePerTurnTriggerEffect(SpellCastTriggerEffect(CardSubtypePredicate(OMEN), "
         "List.of(ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect())))`",
         "`d/DragonsoulProdigy.java`-style `Triggers.youCastSpell().withFilter(CardSubtypePredicate(OMEN))` + "
         "`TriggerLimit.ONCE_EACH_TURN` + `ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect()`"),
        ("`b/BloodAgeMuster.java` + `OncePerTurnTriggerEffect` + ",
         "`b/BloodAgeMuster.java` + `TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN)` + "),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("Triggers.creatureDies(EQUIPPED) TriggeringCardConditionalEffect(CardSubtypePredicate(SAMURAI), "
         "ReturnDyingCreatureToBattlefieldEffect(false))",
         "Triggers.creatureDies(EQUIPPED) ConditionalEffect(TriggeringCardMatches(CardSubtypePredicate(SAMURAI)), "
         "ReturnDyingCreatureToBattlefieldEffect(false))"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`Triggers.creatureDealsCombatDamageToOpponent()` + `TriggeringPermanentControllerConditionalEffect("
         "TriggeringPermanentConditionalEffect(PermanentHasSubtypePredicate(ASSASSIN), CloakTopCardOfDamagedPlayerLibraryEf",
         "`Triggers.creatureDealsCombatDamageToOpponent().withFilter(PermanentAllOfPredicate("
         "PermanentControlledBySourceControllerPredicate, PermanentHasSubtypePredicate(ASSASSIN)))` + "
         "`CloakTopCardOfDamagedPlayerLibraryEf"),
        ("Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) TriggeringPermanentConditionalEffect("
         "PermanentHasSubtypePredicate(BLOOD), GainLifeEffect(1))",
         "Triggers.sacrificed(PermanentAllOfPredicate(PermanentHasSubtypePredicate(BLOOD), PermanentIsTokenPredicate), "
         "ObjectRelation.YOU_CONTROL) GainLifeEffect(1)"),
        ("`Triggers.creatureDies(ObjectRelation.YOU_OWN)` + `TriggeringCardConditionalEffect(CardNotPredicate("
         "CardIsTokenPredicate), ConditionalEffect(SourceCardInGraveyard, CreateTokenEffect.blackZombie(1)))`",
         "`Triggers.dies(PermanentAllOfPredicate(Triggers.CREATURE, PermanentNotPredicate(PermanentIsTokenPredicate)), "
         "ObjectRelation.YOU_OWN)` from the graveyard with `TriggerOptions.DEFAULT.withInterveningIf("
         "SourceCardInGraveyard)` + `CreateTokenEffect.blackZombie(1)`"),
        ("Wrap in `TriggeringCardConditionalEffect` to filter |", "Give the trigger a filter to restrict it |"),
        ("Triggers.creatureBecomesBlocked() `TriggeringCardConditionalEffect(CardSubtypePredicate(SLIVER), "
         "BoostSelfEffect(CreaturesBlockingSource(), CreaturesBlockingSource()))`",
         "`Triggers.creatureBecomesBlocked().withFilter(PermanentHasSubtypePredicate(SLIVER))` "
         "`BoostSelfEffect(CreaturesBlockingSource(), CreaturesBlockingSource())`"),
        ("Triggers.creatureDies(YOU_CONTROL) `TriggeringPermanentConditionalEffect(PermanentPowerAtMostPredicate(1), "
         "MayEffect(RegisterDelayedReturnDyingCreatureUnderControlEffect(true)))`",
         "`Triggers.dies(PermanentAllOfPredicate(Triggers.CREATURE, PermanentPowerAtMostPredicate(1)), YOU_CONTROL)` "
         "`MayEffect(RegisterDelayedReturnDyingCreatureUnderControlEffect(true))`"),
        ("Triggers.creatureDies(YOU_CONTROL) TriggeringCardConditionalEffect(CardSubtypePredicate(GOBLIN), "
         "MayEffect(DealDamageToTargetPlayerOrPlaneswalkerEffect(1)))",
         "Triggers.dies(filter with PermanentHasSubtypePredicate(GOBLIN), YOU_CONTROL) "
         "MayEffect(DealDamageToTargetPlayerOrPlaneswalkerEffect(1))"),
        ("own Triggers.dies(THIS) and `SelfExiledFromBattlefieldEffect` wrap `ConditionalEffect(SourcePowerAtLeast(4), "
         "DestroyTargetPermanentEffect())`",
         "own `Triggers.dies(PermanentPowerAtLeastPredicate(4), THIS)` and `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), "
         "PermanentPowerAtLeastPredicate(4), THIS)` each with `DestroyTargetPermanentEffect()`"),
        ("`GrantEffectToTargetUntilEndOfTurnEffect(Triggers.leavesBattlefield(THIS), SelfExiledFromBattlefieldEffect("
         "CreateTokenEffect(...)))`",
         "`GrantEffectToTargetUntilEndOfTurnEffect(new TriggeredAbility(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), "
         "null, THIS), CreateTokenEffect(...)))`"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("Wrap in `OncePerTurnTriggerEffect` for \"attacks for the first time each turn\" (Aurelia, the Warleader) — "
         "unwrapped at most once per turn per permanent",
         "\"Attacks for the first time each turn\" is `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` "
         "(Aurelia, the Warleader) — at most once per turn per permanent"),
        ("Supports `TriggeringCardConditionalEffect` to filter the attacker;",
         "The trigger's filter is judged on the attacker (Senu: `withFilter(PermanentHasSupertypePredicate(LEGENDARY))`);"),
        ("Supports `TriggeringCardConditionalEffect` (filter by the attacking creature's card) and "
         "`TriggeringPermanentConditionalEffect` (filter by the attacking permanent, e.g. \"",
         "The trigger's filter is judged on the attacking permanent (e.g. \""),
        ("Supports `TriggeringCardConditionalEffect` to filter by the unblocked creature.",
         "The trigger's filter is judged on the unblocked creature."),
        ("Supports `TriggeringPermanentConditionalEffect` to restrict which attackers trigger it (Windreader Sphinx — "
         "flying attackers only).",
         "The trigger's filter restricts which attackers trigger it (Windreader Sphinx — flying attackers only; "
         "\"a creature you control\" is `PermanentControlledBySourceControllerPredicate` in it, Nahiri)."),
        ("Wrap in `TriggeringCardConditionalEffect` to filter by the blocked creature.",
         "The trigger's filter is judged on the blocked creature (Tattered Ratter)."),
        ("`TriggeringPermanentConditionalEffect` may filter the damaging permanent",
         "The trigger's filter may restrict the damaging permanent"),
        ("Use `TriggeringCardConditionalEffect` for a nontoken or other dying-card filter;",
         "Give the trigger a filter for a nontoken or other dying-card restriction;"),
    ],
    "PREDICATES_REFERENCE.md": [
        ("nontoken attacker filter on `Triggers.creatureYouControlAttacks()` via `TriggeringCardConditionalEffect`)",
         "nontoken attacker filter on `Triggers.creatureYouControlAttacks().withFilter(...)`, as "
         "`PermanentNotPredicate(PermanentIsTokenPredicate)`)"),
    ],
    "CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("never an `OncePerTurnTriggerEffect` wrapper.", "never a wrapper effect."),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) TriggeringPermanentConditionalEffect("
         "PermanentIsArtifactPredicate, ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect(1, 0))`",
         "`Triggers.sacrificed(PermanentIsArtifactPredicate, ObjectRelation.YOU_CONTROL) "
         "ChooseCreatureCardInHandOrCreatureYouControlAndApplyPerpetualPowerToughnessEffect(1, 0)`"),
        ("PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(TriggerCondition(Triggers.creatureDies("
         "YOU_CONTROL), GRAVEYARD), TriggeringCardConditionalEffect(CardManaValueGreaterThanSourceManaValuePredicate, "
         "ReturnSourceCardFromGraveyardToBattlefieldEffect(true)))",
         "PerpetuallyGrantTriggeredAbilityToTargetCreatureOrGraveyardCardEffect(TriggerCondition(Triggers.creatureDies("
         "PermanentManaValueGreaterThanSourceCardManaValuePredicate, YOU_CONTROL), GRAVEYARD), "
         "ReturnSourceCardFromGraveyardToBattlefieldEffect(true))"),
        ("`OncePerTurnTriggerEffect(MayEffect(SpellCastTriggerEffect(null, List.of(new "
         "PutTriggeringSpellOnBottomThenRevealEffect(CardNotPredicate(CardTypePredicate(LAND)))))))` | "
         "`Triggers.youCastSpell()`",
         "`MayEffect(PutTriggeringSpellOnBottomThenRevealEffect(CardNotPredicate(CardTypePredicate(LAND))))` with "
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` | `Triggers.youCastSpell().anySpell()`"),
        ("`OncePerTurnTriggerEffect(new MayEffect(CopyControllerCastSpellOnSpellCastEffect.permanentSpellToken("
         "creatureAndFlyingAndNonlegendary, List.of(CardSubtype.SPIRIT), 1, 1)))`",
         "`MayEffect(CopyControllerCastSpellOnSpellCastEffect.permanentSpellToken(creatureAndFlyingAndNonlegendary, "
         "List.of(CardSubtype.SPIRIT), 1, 1))` with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`"),
        ("`SelfExiledFromBattlefieldEffect(MayPayManaEffect(\"{2}\", SearchLibraryEffect(CardTypePredicate("
         "PLANESWALKER)), ...))` | Triggers.leavesBattlefield(THIS)",
         "`MayPayManaEffect(\"{2}\", SearchLibraryEffect(CardTypePredicate(PLANESWALKER)), ...)` | "
         "Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, THIS)"),
        ("and `Triggers.leavesBattlefield(THIS)` + `SelfExiledFromBattlefieldEffect(MayEffect("
         "PutSourceCardFromExileIntoLibraryNFromTopEffect(2)))` (God-Eternal Oketra).",
         "and `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, THIS)` + `MayEffect("
         "PutSourceCardFromExileIntoLibraryNFromTopEffect(2))` (God-Eternal Oketra)."),
        ("`TriggeringCardConditionalEffect(new CardSupertypePredicate(LEGENDARY), new "
         "ReturnSourceCardFromExileToBattlefieldEffect(false, true))` | `Triggers.creatureYouControlAttacksUnblocked() "
         "from exile`",
         "`ReturnSourceCardFromExileToBattlefieldEffect(false, true)` | `Triggers.creatureYouControlAttacksUnblocked()"
         ".withFilter(PermanentHasSupertypePredicate(LEGENDARY))` from exile"),
        ("`TriggeringAbilityFromNamedLandConditionalEffect(\"Labyrinth of Skophos\", MayEffect("
         "SourceFightsTargetCreatureEffect(), \"Have Skophos Maze-Warden fight that creature?\"))` | "
         "`Triggers.creatureBecomesTargetOfSpellOrAbility()` | Skophos Maze-Warden. The trigger-only wrapper filters the "
         "ability source by exact name and land type, requires the watcher to control that source land, and excludes "
         "the watcher itself as the targeted creature.",
         "`MayEffect(SourceFightsTargetCreatureEffect(), \"Have Skophos Maze-Warden fight that creature?\")` | "
         "`Triggers.creatureBecomesTargetOfSpellOrAbility().withFilter(PermanentNotPredicate("
         "PermanentIsSourcePermanentPredicate)).withSource(PermanentAllOfPredicate(PermanentIsLandPredicate, "
         "PermanentNamedPredicate(\"Labyrinth of Skophos\"), PermanentControlledBySourceControllerPredicate))` | "
         "Skophos Maze-Warden. The source filter admits only an ability of a matching permanent, judged from the "
         "watcher's point of view, and the filter excludes the watcher itself as the targeted creature."),
        ("`Triggers.creatureDies(ANY)` `TriggeringCardConditionalEffect(CardColorPredicate(WHITE), "
         "RegisterDelayedReturnDyingCreatureUnderControlEffect(... returnUnderOwnersControl=true))` + "
         "`TriggeringCardConditionalEffect(CardNotPredicate(CardColorPredicate(WHITE)), "
         "RegisterDelayedReturnCardFromGraveyardToHandEffect(null))`",
         "`Triggers.creatureDies(PermanentColorInPredicate(WHITE), ANY)` `RegisterDelayedReturnDyingCreatureUnderControlEffect("
         "... returnUnderOwnersControl=true)` + `Triggers.creatureDies(PermanentNotPredicate(PermanentColorInPredicate("
         "WHITE)), ANY)` `RegisterDelayedReturnCardFromGraveyardToHandEffect(null)` (the planar reader judges the color "
         "as the creature last existed)"),
        ("`Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY)` + `TriggeringArtifactControllerConditionalEffect("
         "MayPayManaEffect(\"{3}\", DrawCardEffect), true)` | trigger | Urza's Miter (ATQ 76). The wrapper's non-sacrifice "
         "condition distinguishes",
         "`Triggers.diesWithoutBeingSacrificed(Triggers.ARTIFACT, ObjectRelation.YOU_CONTROL)` + `MayPayManaEffect("
         "\"{3}\", DrawCardEffect)` | trigger | Urza's Miter (ATQ 76). The trigger's causes (every one but a sacrifice) "
         "distinguish"),
        ("`TriggeringCardConditionalEffect(CardSubtypePredicate(SUBTYPE), BoostSelfEffect(new CreaturesBlockingSource(), "
         "new CreaturesBlockingSource()))` | Triggers.creatureBecomesBlocked()",
         "`BoostSelfEffect(new CreaturesBlockingSource(), new CreaturesBlockingSource())` | "
         "Triggers.creatureBecomesBlocked().withFilter(PermanentHasSubtypePredicate(SUBTYPE))"),
        ("(self-scoped to \"it\"; wrap in `TriggeringCardConditionalEffect` to filter the blocked creature)",
         "(self-scoped to \"it\"; `.withFilter(...)` on the trigger filters the blocked creature, Tattered Ratter)"),
        ("`CombatBlockService` unwraps `TriggeringPermanentConditionalEffect` per blocker, so the trigger only goes on "
         "the stack for matching blockers;",
         "`CombatBlockService` judges the filter per blocker, so the trigger only goes on the stack for matching "
         "blockers;"),
        ("wrap in `TriggeringPermanentConditionalEffect` to filter by the renowned creature.",
         "give the trigger a filter to restrict the renowned creature."),
        ("`Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, THIS)` + `SelfExiledFromBattlefieldEffect("
         "CreateTokenEffect.ofPowerstoneToken(new Fixed(1)))` + `Triggers.putIntoGraveyardOrExileFromBattlefield("
         "Triggers.NONTOKEN_ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL)`",
         "one `TriggeredAbility` of `Triggers.putIntoGraveyardOrExileFromBattlefield(Triggers.NONTOKEN_ARTIFACT, "
         "ObjectRelation.ANOTHER_YOU_CONTROL)` and `Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, THIS)` + "
         "`Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, THIS)` with `CreateTokenEffect.ofPowerstoneToken(new "
         "Fixed(1))`"),
        ("`Triggers.creatureDealsCombatDamageToOpponent()` + `TriggeringPermanentControllerConditionalEffect("
         "TriggeringPermanentConditionalEffect(PermanentHasSubtypePredicate(ASSASSIN), "
         "CloakTopCardOfDamagedPlayerLibraryEffect()))`",
         "`Triggers.creatureDealsCombatDamageToOpponent().withFilter(PermanentAllOfPredicate("
         "PermanentControlledBySourceControllerPredicate, PermanentHasSubtypePredicate(ASSASSIN)))` + "
         "`CloakTopCardOfDamagedPlayerLibraryEffect()`"),
        ("the `TriggeringCardConditionalEffect` is what narrows it.", "the trigger's filter is what narrows it."),
        ("+ `TriggeringCardConditionalEffect` with the same predicate and token effect",
         "+ `ConditionalEffect(TriggeringCardMatches(same predicate), token effect)`, the type re-checked as it "
         "resolves"),
        ("with the ally trigger gated by `TriggeringPermanentConditionalEffect(PermanentNotPredicate("
         "PermanentIsLandPredicate()), ...)`",
         "with the ally trigger's filter `PermanentNotPredicate(PermanentIsLandPredicate())`"),
        ("`TriggeringPermanentConditionalEffect(new PermanentToughnessAtLeastPredicate(4), new MayEffect(new "
         "ExileTriggeringCreatureAndTrackWithSourceEffect(), prompt))` | `Triggers.creatureDies(YOU_CONTROL)`",
         "`MayEffect(new ExileTriggeringCreatureAndTrackWithSourceEffect(), prompt)` | `Triggers.dies("
         "PermanentAllOfPredicate(Triggers.CREATURE, PermanentToughnessAtLeastPredicate(4)), YOU_CONTROL)`"),
        ("`TriggeringCardConditionalEffect(CardNotPredicate(CardIsTokenPredicate), ConditionalEffect("
         "SourceCardInGraveyard, CreateTokenEffect.blackZombie(1)))` + `ConditionalEffect(SourceCardInGraveyard, "
         "ExileSourceCardFromGraveyardEffect())`",
         "`CreateTokenEffect.blackZombie(1)` on a nontoken-creature filter + `ExileSourceCardFromGraveyardEffect()`, "
         "each with `TriggerOptions.DEFAULT.withInterveningIf(SourceCardInGraveyard)`"),
        ("`TriggeringPermanentConditionalEffect(anotherNontokenArtifactOrCreature, ExileSacrificedPermanentWithSourceEffect())`",
         "`Triggers.sacrificed(anotherNontokenArtifactOrCreature, ANOTHER_YOU_CONTROL)` + "
         "`ExileSacrificedPermanentWithSourceEffect()`"),
        ("`Triggers.creatureDies(ANOTHER_YOU_CONTROL)` + `TriggeringCardConditionalEffect(new CardSubtypePredicate("
         "subtype), new MayEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1), \"...\"))`",
         "`Triggers.dies(filter with PermanentHasSubtypePredicate(subtype), ANOTHER_YOU_CONTROL)` + `new MayEffect(new "
         "DealDamageToTargetPlayerOrPlaneswalkerEffect(1), \"...\")`"),
        ("`TriggeringCardConditionalEffect(new CardSubtypePredicate(SAMURAI), new "
         "ReturnDyingCreatureToBattlefieldEffect(false))` | `Triggers.creatureDies(EQUIPPED)`",
         "`ConditionalEffect(new TriggeringCardMatches(new CardSubtypePredicate(SAMURAI)), new "
         "ReturnDyingCreatureToBattlefieldEffect(false))` | `Triggers.creatureDies(EQUIPPED)`"),
        ("`TriggeringCardConditionalEffect(CardAnyOfPredicate(CardSubtypePredicate(ISLAND), CardColorPredicate(BLUE)), "
         "SacrificePermanentsEffect(",
         "the trigger's filter (Island or blue) + `SacrificePermanentsEffect("),
        ("`TriggeringPermanentConditionalEffect(new PermanentPowerAtMostPredicate(1), new MayEffect(new "
         "RegisterDelayedReturnDyingCreatureUnderControlEffect(true), \"...\"))` | `Triggers.creatureDies(YOU_CONTROL)` "
         "| Shirei, Shizo's Caretaker. The conditional reads the dying permanent's last-known power;",
         "`new MayEffect(new RegisterDelayedReturnDyingCreatureUnderControlEffect(true), \"...\")` | `Triggers.dies("
         "PermanentAllOfPredicate(Triggers.CREATURE, PermanentPowerAtMostPredicate(1)), YOU_CONTROL)` | Shirei, Shizo's "
         "Caretaker. The filter reads the dying permanent's last-known power;"),
        ("`OncePerTurnTriggerEffect(UntapPermanentsEffect(CONTROLLED, PermanentIsCreaturePredicate))` + "
         "`OncePerTurnTriggerEffect(AdditionalCombatPhaseEffect(1))` | Triggers.attacks() | Aurelia, the Warleader (GTC). "
         "Flying/vigilance/haste are auto-loaded. The `OncePerTurnTriggerEffect` wrapper is unwrapped at "
         "`Triggers.attacks()` collection at most once per turn per permanent",
         "`UntapPermanentsEffect(CONTROLLED, PermanentIsCreaturePredicate)` + `AdditionalCombatPhaseEffect(1)` with "
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` | Triggers.attacks() | Aurelia, the Warleader "
         "(GTC). Flying/vigilance/haste are auto-loaded. The limit is judged at `Triggers.attacks()` collection at most "
         "once per turn per permanent"),
        ("`OncePerTurnTriggerEffect.markOnAcceptance(new MayEffect(CopyControllerCastSpellOnSpellCastEffect."
         "fromOutsideHandWithPermanentSpellHaste(null), \"Copy that spell?\"))`",
         "`new MayEffect(CopyControllerCastSpellOnSpellCastEffect.fromOutsideHandWithPermanentSpellHaste(null), \"Copy "
         "that spell?\")` with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`"),
        ("`Triggers.creatureAttacks()` + `TriggeringPermanentControllerConditionalEffect(TriggeringPermanentConditionalEffect("
         "PermanentIsEquippedPredicate, ExileTopCardMayPlayThisTurnEffect(1, false, CardSubtypePredicate(EQUIPMENT))))`",
         "`Triggers.creatureAttacks().withFilter(PermanentAllOfPredicate(PermanentControlledBySourceControllerPredicate, "
         "PermanentIsEquippedPredicate))` + `ExileTopCardMayPlayThisTurnEffect(1, false, CardSubtypePredicate(EQUIPMENT))`"),
        ("`Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL) TriggeringPermanentConditionalEffect("
         "PermanentHasSubtypePredicate(BLOOD), SequenceEffect.of(LoseLifeEffect(1, EACH_OPPONENT), GainLifeEffect(1)))`",
         "`Triggers.sacrificed(PermanentHasSubtypePredicate(BLOOD), ObjectRelation.YOU_CONTROL) SequenceEffect.of("
         "LoseLifeEffect(1, EACH_OPPONENT), GainLifeEffect(1))`"),
    ],
}

SKILLS = {
    os.path.join(ROOT, ".claude", "skills", "implement-card", "SKILL.md"): [
        ("in `ConditionalEffect`, `TriggeringPermanentConditionalEffect` or `OncePerTurnTriggerEffect`:",
         "in `ConditionalEffect` or another wrapper:"),
    ],
    os.path.join(ROOT, ".claude", "skills", "review-card", "SKILL.md"): [],
}

# The skills' "never wrap … in X / Y / Z" lists drop the deleted classes (OncePerTurnTriggerEffect still exists).
SKILL_LIST_NAMES = ("TriggeringCardConditionalEffect", "TriggeringPermanentConditionalEffect",
                    "TriggeringPermanentControllerConditionalEffect", "OncePerTurnPerCreatureTriggerEffect",
                    "SelfExiledFromBattlefieldEffect")


def drop_from_lists(text):
    import re
    text = text.replace("`TriggeringArtifactControllerConditionalEffect` (but for \"if it wasn't sacrificed\")",
                        "`TriggeringArtifactControllerConditionalEffect`")
    text = text.replace("(a granted ability's per-grant limit, `OncePerTurnTriggerEffect.perGrant`, stays)",
                        "(a granted ability keeps its limit for each granting permanent)")
    names = SKILL_LIST_NAMES + ("TriggeringArtifactControllerConditionalEffect",)
    for name in names:
        item = "`" + name + "`"
        for sep in (" / ", ", "):
            text = text.replace(item + sep, "").replace(sep + item, "")
        text = re.sub(r"(in|never|never write|never a|never an) " + re.escape(item) + r" or ", r"\1 ", text)
        text = text.replace(item + " or ", "").replace(" or " + item, "")
        text = text.replace("never " + item + ";", "never a wrapper effect;")
    text = text.replace("a a wrapper effect", "a wrapper effect")
    text = text.replace("a wrapper effect / a wrapper effect", "a wrapper effect")
    return text


def entry_of_deleted(line):
    if "NykthosParagonLifeGainEffect" in line:
        return False
    stripped = line.lstrip()
    for name in DELETED:
        if stripped.startswith("- `" + name) or stripped.startswith("| `" + name + "`") \
                or stripped.startswith("- `" + name + "."):
            return True
    return False


def rewrite_reference(path, insert_line):
    """Drops the deleted classes' entries and puts insert_line where the first one was."""
    text = open(path).read()
    if insert_line in text:
        return text
    out = []
    inserted = False
    for line in text.split("\n"):
        if entry_of_deleted(line):
            if not inserted:
                out.append(insert_line)
                inserted = True
            continue
        out.append(line)
    return "\n".join(out)


def apply(path, replacements, text=None):
    if text is None:
        text = open(path).read()
    for old, new in replacements:
        if old in text:
            text = text.replace(old, new)
        elif new not in text:
            sys.exit(os.path.basename(path) + ": neither the old nor the new text is there:\n" + old[:160])
    return text


def main():
    for name in sorted(set(REPLACEMENTS) | {"EFFECTS_QUICK_REFERENCE.md", "EFFECTS_INDEX.md"}):
        path = os.path.join(DOCS, name)
        text = None
        if name == "EFFECTS_QUICK_REFERENCE.md":
            text = rewrite_reference(path, LIMITS_BULLET + "\n" + FILTERS_BULLET)
        elif name == "EFFECTS_INDEX.md":
            text = rewrite_reference(path, INDEX_LIMITS_ROW + "\n" + INDEX_FILTERS_ROW)
        text = apply(path, REPLACEMENTS.get(name, []), text)
        with open(path, "w") as f:
            f.write(text)
    for path, replacements in SKILLS.items():
        text = drop_from_lists(apply(path, replacements))
        with open(path, "w") as f:
            f.write(text)
    for directory in (DOCS, os.path.join(ROOT, ".claude")):
        for base, _, files in os.walk(directory):
            for file in files:
                if not file.endswith(".md"):
                    continue
                for number, line in enumerate(open(os.path.join(base, file)), 1):
                    for deleted in DELETED[:-1]:
                        if deleted in line:
                            print("left:", file, number, deleted)


if __name__ == "__main__":
    main()
