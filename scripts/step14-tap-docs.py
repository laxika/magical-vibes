#!/usr/bin/env python3
"""Step 14, run 12 of effect-slot-refactor.md: the agent-docs passages that told card authors to wrap a tap or untap
ability's effects in TriggeringPermanentConditionalEffect, OncePerTurnTriggerEffect, the global
OncePerTurnPerCreatureTriggerEffect, an intervening-if ConditionalEffect or TriggerMode.ONCE_PER_BATCH now show the
trigger's filter (TapTrigger.withFilter), the new factories (becomesTapped(), the "first time during your turn" ones)
and TriggerOptions, as scripts/step14-tap-options.py converted the cards.

Each replacement is exact and must match once (or, flagged, at least once); one that finds nothing stops the script, unless its new text is already there (so running
it again changes nothing).

Usage: python3 scripts/step14-tap-docs.py [--dry-run]
"""
import sys

OTEM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
EQR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
EI = "agent-docs/EFFECTS_INDEX.md"
AAG = "agent-docs/ACTIVATED_ABILITY_GUIDE.md"
TST = "agent-docs/TRIGGER_SLOT_TARGETING.md"

REPLACEMENTS = [
    (OTEM,
     "`GrantTriggeredAbilityEffect(Triggers.permanentYouControlBecomesTapped(), TriggeringPermanentConditionalEffect("
     "PermanentIsSourceCardPredicate, BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect(2, 0, "
     "Set.of(UNDYING))), ",
     "`GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.becomesTapped(), "
     "BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect(2, 0, Set.of(UNDYING))), "),
    (OTEM,
     "| STATIC + Triggers.permanentYouControlBecomesTapped() | Haunted One",
     "| STATIC + Triggers.becomesTapped() | Haunted One"),
    (OTEM,
     "`TriggeringPermanentConditionalEffect(PermanentIsSourceCardPredicate, "
     "PayXManaRevealTopCardsToHandRestToBottomRandomEffect(CardAnyOfPredicate(DWARF, VEHICLE)))` | "
     "Triggers.permanentYouControlBecomesTapped() | Depala",
     "`PayXManaRevealTopCardsToHandRestToBottomRandomEffect(CardAnyOfPredicate(DWARF, VEHICLE))` | "
     "Triggers.becomesTapped() | Depala"),
    (OTEM,
     "`RegisterGlobalTriggeredAbilityUntilNextTurnEffect(Triggers.permanentYouControlBecomesTapped(), "
     "TriggeringPermanentConditionalEffect(creature, DestroyReferencedPermanentEffect(TRIGGERING)))` + the matching "
     "`Triggers.permanentOpponentControlsBecomesTapped()` registration | SPELL | Don't Move. The two "
     "controller-relative tap registrations cover creatures controlled by either player;",
     "`RegisterGlobalTriggeredAbilityUntilNextTurnEffect(new TriggeredAbility(Triggers.creatureBecomesTapped(), "
     "DestroyReferencedPermanentEffect(TRIGGERING)))` | SPELL | Don't Move. The global creature-tap trigger covers "
     "creatures controlled by either player;"),
    (OTEM,
     "+ `Triggers.permanentYouControlBecomesTapped()` `TriggeringPermanentConditionalEffect(PermanentIsSourceCardPredicate, "
     "SequenceEffect.of(DealDamageToPlayersEffect(2, CONTROLLER), DealDamageToEachMatchingPermanentEffect(2, ownCreature, "
     "ALL_PLAYERS)))` | ACTIVATED_ABILITY + Triggers.permanentYouControlBecomesTapped() | Sorrow's Path",
     "+ `Triggers.becomesTapped()` `SequenceEffect.of(DealDamageToPlayersEffect(2, CONTROLLER), "
     "DealDamageToEachMatchingPermanentEffect(2, ownCreature, ALL_PLAYERS))` | ACTIVATED_ABILITY + "
     "Triggers.becomesTapped() | Sorrow's Path"),
    (OTEM,
     "`TriggeringPermanentConditionalEffect(new PermanentHasSubtypePredicate(subtype), new MayEffect(new "
     "GainLifeEffect(1), \"prompt\"))` | `Triggers.permanentYouControlBecomesTapped()` | Judge of Currents.",
     "`new MayEffect(new GainLifeEffect(1), \"prompt\")` | `Triggers.permanentYouControlBecomesTapped().withFilter(new "
     "PermanentHasSubtypePredicate(subtype))` | Judge of Currents."),
    (OTEM,
     "; the conditional filters by the tapped permanent. Includes the source itself if it matches",
     "; the trigger's filter judges the tapped permanent. Includes the source itself if it matches"),
    (OTEM,
     "`TriggeringPermanentConditionalEffect(new PermanentIsSourceCardPredicate(), new ExileTopCardsToSourceEffect(N, "
     "true, false, LibraryScope.TARGET_OPPONENT))` | Triggers.permanentYouControlBecomesTapped() | Grimoire Thief. The "
     "conditional restricts the shared tap trigger to the source itself; two-player",
     "`new ExileTopCardsToSourceEffect(N, true, false, LibraryScope.TARGET_OPPONENT)` | Triggers.becomesTapped() | "
     "Grimoire Thief. Two-player"),
    (OTEM,
     "+ `TriggeringPermanentConditionalEffect(PermanentIsCreaturePredicate, "
     "PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE))` | Triggers.enters(ObjectRelation.THIS) + "
     "Triggers.youTapPermanentOpponentControls() |",
     "+ `PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE)` | Triggers.enters(ObjectRelation.THIS) + "
     "Triggers.youTapPermanentOpponentControls().withFilter(PermanentIsCreaturePredicate) |"),
    (OTEM,
     "+ `TriggeringPermanentConditionalEffect(new PermanentHasSourceChosenSubtypePredicate(), new GainLifeEffect(1))` | "
     "STATIC + Triggers.permanentOpponentControlsBecomesTapped() |",
     "+ `new GainLifeEffect(1)` | STATIC + Triggers.permanentOpponentControlsBecomesTapped().withFilter(new "
     "PermanentHasSourceChosenSubtypePredicate()) |"),
    (OTEM,
     "`TriggeringPermanentConditionalEffect(new PermanentHasSubtypePredicate(CardSubtype.X), new MayEffect(new "
     "GainLifeEffect(N), prompt))` | Triggers.permanentOpponentControlsBecomesTapped() |",
     "`new MayEffect(new GainLifeEffect(N), prompt)` | Triggers.permanentOpponentControlsBecomesTapped().withFilter(new "
     "PermanentHasSubtypePredicate(CardSubtype.X)) |"),
    (OTEM,
     "+ `Triggers.permanentYouControlBecomesTapped()` and `Triggers.permanentOpponentControlsBecomesTapped()` "
     "`TriggeringPermanentConditionalEffect(AllOf(IsLand, HasCounters(MINE)), DestroyReferencedPermanentEffect(TRIGGERING))` +",
     "+ one ability on `List.of(Triggers.permanentYouControlBecomesTapped().withFilter(minedLand), "
     "Triggers.permanentOpponentControlsBecomesTapped().withFilter(minedLand))` (minedLand = AllOf(IsLand, "
     "HasCounters(MINE))) `DestroyReferencedPermanentEffect(TRIGGERING)` +"),
    (OTEM,
     "Wrap in `TriggeringPermanentConditionalEffect(predicate, effect)` to filter by the untapped permanent (e.g. \"a "
     "Merfolk you control\").",
     "Filter the untapped permanent with the trigger's `.withFilter(predicate)` (e.g. \"a Merfolk you control\")."),
    (OTEM,
     "`GrantEffectToTargetUntilEndOfTurnEffect(Triggers.permanentYouControlBecomesTapped(), "
     "TriggeringPermanentConditionalEffect(PermanentIsSourceCardPredicate, "
     "SourcePermanentDealsPowerDamageToUpToOneTargetCreatureEffect()))`",
     "`GrantEffectToTargetUntilEndOfTurnEffect(new TriggeredAbility(Triggers.becomesTapped(), "
     "SourcePermanentDealsPowerDamageToUpToOneTargetCreatureEffect()))`"),
    (OTEM,
     "`TriggeringPermanentConditionalEffect(PermanentIsCreaturePredicate(), OncePerTurnPerCreatureTriggerEffect("
     "UntapPermanentsEffect(TapUntapScope.TRIGGERING), true))` | `Triggers.permanentYouControlBecomesTapped()` | "
     "Captain America",
     "`UntapPermanentsEffect(TapUntapScope.TRIGGERING)` | "
     "`Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn()` | Captain America"),
    (OTEM,
     "`TriggeringPermanentConditionalEffect(PermanentAnyOf(HasAnySubtype(SWAMP,MOUNTAIN), ColorIn(BLACK,RED)), "
     "DealDamageToPlayersEffect(1, TRIGGERING_PERMANENT_CONTROLLER))` on **both** tap slots + UPKEEP "
     "`CumulativeUpkeepEffect(\"{W}\")` | both tap slots + UPKEEP |",
     "`DealDamageToPlayersEffect(1, TRIGGERING_PERMANENT_CONTROLLER)` on one ability with **both** tap conditions, "
     "each `.withFilter(PermanentAnyOf(HasAnySubtype(SWAMP,MOUNTAIN), ColorIn(BLACK,RED)))`, + UPKEEP "
     "`CumulativeUpkeepEffect(\"{W}\")` | `List.of(Triggers.permanentYouControlBecomesTapped().withFilter(f), "
     "Triggers.permanentOpponentControlsBecomesTapped().withFilter(f))` + UPKEEP |"),
    (OTEM,
     "`ConditionalEffect(new SourceIsTapped(), SequenceEffect.of(AddOneOfEachManaTypeProducedByLandEffect(false), "
     "DealDamageOnLandTapEffect(1, null, true)))` | Triggers.playerTapsLandForMana() | Barbflare",
     "`SequenceEffect.of(AddOneOfEachManaTypeProducedByLandEffect(false), DealDamageOnLandTapEffect(1, null, true))` "
     "with `TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped())` | Triggers.playerTapsLandForMana() | "
     "Barbflare"),
    ("agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "Triggers.playerTapsLandForMana() `ConditionalEffect(new SourceIsTapped(), SequenceEffect.of("
     "AddOneOfEachManaTypeProducedByLandEffect(false), DealDamageOnLandTapEffect(1, null, true)))` — the source-tapped "
     "intervening-if gates",
     "Triggers.playerTapsLandForMana() `TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped())` "
     "`SequenceEffect.of(AddOneOfEachManaTypeProducedByLandEffect(false), DealDamageOnLandTapEffect(1, null, true))` — "
     "the source-tapped intervening \"if\" gates"),
    ("agent-docs/CARD_PATTERNS_PERMANENTS_ARTIFACTS.md",
     "Triggers.permanentBecomesUntapped() TriggeringPermanentConditionalEffect(source attachment host, "
     "ConditionalEffect.unless(SourceCounterThreshold(1, BAIT), SequenceEffect(RemoveCounterFromSourceEffect(BAIT, 1), "
     "CreateTokenEffect(Fish 1/1 blue))))",
     "Triggers.permanentBecomesUntapped().withFilter(PermanentIsHostOfSourceAuraPredicate) "
     "ConditionalEffect.unless(SourceCounterThreshold(1, BAIT), SequenceEffect(RemoveCounterFromSourceEffect(BAIT, 1), "
     "CreateTokenEffect(Fish 1/1 blue)))"),
    (EQR,
     "The `(CardEffect, true)` form uses the creature's global turn-wide event history, regardless of which source was "
     "present earlier (Captain America, Living Legend).",
     "Don't author the `(CardEffect, true)` form either: on a tap trigger \"for the first time during your turn\" is "
     "`Triggers.becomesTappedForFirstTimeDuringYourTurn()` / "
     "`Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn()` (Tale of Katara and Toph, Captain "
     "America, Living Legend), and the legacy view gives the tap collector this global form, which uses the "
     "creature's turn-wide tap history regardless of which source was present earlier."),
    (EQR,
     "(Interface Ace, Deathless Knight; creature-death triggers use",
     "(Deathless Knight; tap triggers (Interface Ace, Nanoform Sentinel, Sharae of Numbing Depths) and creature-death "
     "triggers use"),
    (EQR,
     "(Interface Ace; creature-death triggers use",
     "(tap triggers (Interface Ace, Nanoform Sentinel) and creature-death triggers use"),
    (EQR,
     " Becomes-tapped collectors unwrap it after event-subject filters and before resolution-time conditionals.",
     " The legacy view gives the becomes-tapped collectors a tap ability's limit as this wrapper, keyed, inside the "
     "tapped permanent's filter and outside the intervening \"if\".", True),
    (EI,
     "Used by Interface Ace and Deathless Knight; creature-death triggers use",
     "Used by Deathless Knight; tap triggers (Interface Ace, Nanoform Sentinel) and creature-death triggers use"),
    (EI,
     "`global=true` instead uses the creature's turn-wide event history, including events before the watcher entered.",
     "`global=true` instead uses the creature's turn-wide event history, including events before the watcher "
     "entered; don't author it: \"for the first time during your turn\" is "
     "`Triggers.becomesTappedForFirstTimeDuringYourTurn()` / "
     "`creatureYouControlBecomesTappedForFirstTimeDuringYourTurn()`, which the legacy view gives the tap collector in "
     "this form."),
    (EI,
     "on its \"becomes tapped\" trigger, wrapped in `trigger.withFilter(new PermanentIsSourceCardPredicate()), …`;",
     "on its `Triggers.becomesTapped()` trigger;"),
    (EI,
     "and support `TriggeringPermanentConditionalEffect` filters.",
     "and read a filter on the trigger (`.withFilter(p)`) through the legacy view."),
    (AAG,
     "Wrap in `TriggeringPermanentConditionalEffect` to filter the tapped permanent. Used by Solitary Sanctuary",
     "Filter the tapped permanent with the trigger's `.withFilter(p)`; \"one or more\" is `TriggerOptions.ONE_OR_MORE`. "
     "Used by Solitary Sanctuary, Sharae of Numbing Depths"),
    (AAG,
     "wrap in `TriggeringPermanentConditionalEffect` to filter the tapped permanent.",
     "filter the tapped permanent with the trigger's `.withFilter(p)`, and write \"whenever a [quality] permanent "
     "becomes tapped\" as one ability with both conditions (`addTrigger(List.of(Triggers.permanentYouControlBecomesTapped()"
     ".withFilter(p), Triggers.permanentOpponentControlsBecomesTapped().withFilter(p)), effect)`: Royal Decree, Haunting "
     "Wind). \"Whenever this permanent becomes tapped\" is `Triggers.becomesTapped()`, which the "
     "`permanentYouControlBecomesTapped()` collector reads."),
    (TST,
     "The tap collector\nalso supports global per-creature first-event wrappers: it checks",
     "The tap collector\nalso reads \"for the first time during your turn\" (`Triggers.becomesTappedForFirstTimeDuringYourTurn()`,\n"
     "`creatureYouControlBecomesTappedForFirstTimeDuringYourTurn()`, the global per-creature first-event wrapper of the\n"
     "legacy view): it checks"),
    (TST,
     "`checkBecomesUntappedTriggers` call sites. Wrap in `TriggeringPermanentConditionalEffect` to filter by the\n"
     "untapped permanent),",
     "`checkBecomesUntappedTriggers` call sites. Filter the untapped permanent with the trigger's `.withFilter(p)`),"),
    (TST,
     "tapped→untapped on any battlefield. Wrap in `TriggeringPermanentConditionalEffect` to filter by the\n"
     "untapped permanent.",
     "tapped→untapped on any battlefield. Filter the untapped permanent with the trigger's `.withFilter(p)`."),
    ("agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md",
     "  - `addTrigger(Triggers.permanentYouControlBecomesTapped(), TriggerMode.ONCE_PER_BATCH, effect)`",
     "  - `addTrigger(Triggers.permanentYouControlBecomesTapped(), TriggerOptions.ONE_OR_MORE, effect)` (\"whenever one "
     "or more creatures you control become tapped\": `.withFilter(creature)`)"),
]


def main():
    dry = "--dry-run" in sys.argv
    texts = {}
    changed = 0
    for path, old, new, *every in REPLACEMENTS:
        text = texts.setdefault(path, open(path).read())
        count = text.count(old)
        if count == 0 and new in text:
            continue
        if count != 1 and not (every and count > 1):
            raise SystemExit("%s: %d matches for %r" % (path, count, old[:80]))
        texts[path] = text.replace(old, new)
        changed += 1
    for path, text in texts.items():
        if not dry:
            open(path, "w").write(text)
    print("%d passages" % changed, file=sys.stderr)


if __name__ == "__main__":
    main()
