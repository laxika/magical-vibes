#!/usr/bin/env python3
"""Step 14, run 9 of effect-slot-refactor.md: the agent-docs rows of the counter abilities show the trigger filter,
the "you put" / "you remove" / "each counter" events and TriggerOptions instead of the trigger-only wrappers, and the
wrapper class entries say not to author them on a counter trigger. Exact replacements; one that doesn't match stops the
script.

Usage: python3 scripts/step14-counter-docs.py [--dry-run]
"""
import sys

ONCE = "TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)"
DONT = ("Don't author it on a counter trigger (step 14 of effect-slot-refactor.md): write %s. ")

EDITS = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.countersPutOnCreatureYouControl() OncePerTurnTriggerEffect(SeekLibraryAndPerpetuallyBoostSoughtCardEffect(CardTypePredicate(CREATURE), 1, 1))`",
         "`Triggers.plusOneCountersPutOnCreatureYouControl()` + `%s` + `SeekLibraryAndPerpetuallyBoostSoughtCardEffect(CardTypePredicate(CREATURE), 1, 1)`" % ONCE),
        ("`Triggers.plusOneCountersPutOnCreatureYouControl() from the graveyard` + `MayEffect(ConditionalEffect(new SourceCardInGraveyard(), new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0)))`",
         "`Triggers.plusOneCountersPutOnCreatureYouControl() from the graveyard` + `TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard())` + `MayEffect(new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0))`"),
        ("`OncePerTurnTriggerEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE))` | `Triggers.youPutPlusOneCountersOnAnotherCreature()`",
         "`%s` + `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)` | `Triggers.youPutPlusOneCountersOnAnotherCreature()`" % ONCE),
        ("`TriggeringPermanentConditionalEffect(PermanentIsColorlessPredicate, MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE), prompt))` | `Triggers.youPutPlusOneCountersOnAnotherCreature()`",
         "`MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE), prompt)` | `Triggers.youPutPlusOneCountersOnAnotherCreature().withFilter(new PermanentIsColorlessPredicate())`"),
        ("`OncePerTurnTriggerEffect(new DrawCardEffect(1))` | `Triggers.plusOneCountersPutOnHumanYouControl()`",
         "`%s` + `new DrawCardEffect(1)` | `Triggers.plusOneCountersPutOnHumanYouControl()`" % ONCE),
        ("`OncePerTurnTriggerEffect(new MayEffect(new DrawCardEffect(new EventValue()), \"Draw that many cards?\"))` | `Triggers.youPutPlusOneCountersOnCreatureYouControlOnceEachTurn()`",
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` + `new MayEffect(new DrawCardEffect(new EventValue()), \"Draw that many cards?\")` | `Triggers.youPutPlusOneCountersOnCreatureYouControl()`"),
        ("`ConditionalEffect(SourceHasChosenMode(\"Sultai\"), OncePerTurnTriggerEffect(DrawCardEffect(1)))` | `Triggers.countersPutOnCreatureYouControl()`",
         "`TriggerOptions.DEFAULT.withInterveningIf(new SourceHasChosenMode(\"Sultai\")).withLimit(TriggerLimit.ONCE_EACH_TURN)` + `DrawCardEffect(1)` | `Triggers.countersPutOnCreatureYouControl()`"),
        ("`YouPutCounterOnControlledCreatureTriggerEffect(OncePerTurnTriggerEffect(DrawCardEffect(1)))` | `Triggers.countersPutOnCreatureYouControl()` | The outer wrapper restricts the generic creature-counter watcher to placements caused by the watcher controller; the inner once-per-turn wrapper then limits the draw.",
         "`%s` + `DrawCardEffect(1)` | `Triggers.youPutCountersOnCreatureYouControl()` | Only placements the watcher's controller makes count; the option limits the draw." % ONCE),
        ("`OncePerTurnPerCreatureTriggerEffect(PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, PLUS_ONE_PLUS_ONE))` | `Triggers.countersPutOnCreatureYouControl()`",
         "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE)` + `PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, PLUS_ONE_PLUS_ONE)` | `Triggers.countersPutOnCreatureYouControl()`"),
        ("`YouRemoveTimeCounterFromExiledCardTriggerEffect(new SurveilEffect(1))` | Triggers.timeCounterRemovedFromThisInExile()",
         "`new SurveilEffect(1)` | Triggers.youRemoveTimeCounterFromThisInExile()"),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("fires once per placement event and supports `OncePerTurnTriggerEffect` (Knight of Wundagore).",
         "fires once per placement event; \"triggers only once each turn\" is `%s` (Knight of Wundagore)." % ONCE),
        ("and is typically paired with `OncePerTurnTriggerEffect` (Cloaked Cadet).",
         "and is typically paired with `%s` (Cloaked Cadet)." % ONCE),
        ("- `OncePerTurnPerCreatureTriggerEffect(CardEffect)` — on `Triggers.countersPutOnCreatureYouControl()`, fires",
         "- `OncePerTurnPerCreatureTriggerEffect(CardEffect)` — " + DONT % "`TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE)`" + "On `Triggers.countersPutOnCreatureYouControl()`, fires"),
        ("- `YouPutCounterOnControlledCreatureTriggerEffect(CardEffect)` — on `Triggers.countersPutOnCreatureYouControl()`, filters the trigger to counter placements caused by the watcher controller on a creature that controller controls. Compose it with `OncePerTurnTriggerEffect` for \"Whenever you put a counter on a creature you control\" wording (Mikey & Leo, Chaos & Order).",
         "- `YouPutCounterOnControlledCreatureTriggerEffect(CardEffect)` — " + DONT % "the trigger `Triggers.youPutCountersOnCreatureYouControl()` (\"whenever you put a counter on a creature you control\", Mikey & Leo, Chaos & Order) or `Triggers.youPutPlusOneCountersOnThis()` (Exemplar of Light, Blaster)" + "The counter collectors read it from the legacy view of those triggers: it keeps only placements caused by the watcher's controller."),
        ("- `YouRemoveTimeCounterFromExiledCardTriggerEffect(CardEffect)` - `Triggers.timeCounterRemovedFromThisInExile()` wrapper firing only when the card's controller removes the time counter (Watcher of Hours)",
         "- `YouRemoveTimeCounterFromExiledCardTriggerEffect(CardEffect)` - " + DONT % "the trigger `Triggers.youRemoveTimeCounterFromThisInExile()` (Watcher of Hours)" + "The exiled time-counter collector reads it from that trigger's legacy view: only the card's controller removing the counter counts."),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("Don't author it on a damage trigger: write `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE)`",
         "Don't author it on a damage or counter trigger: write `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE)`"),
        ("| `YouPutCounterOnControlledCreatureTriggerEffect` | `(CardEffect wrapped)` | `Triggers.countersPutOnCreatureYouControl()` wrapper that keeps only placements caused by the watcher controller on a creature that controller controls. Compose with `OncePerTurnTriggerEffect` for \"Whenever you put a counter on a creature you control\" wording (Mikey & Leo, Chaos & Order) |",
         "| `YouPutCounterOnControlledCreatureTriggerEffect` | `(CardEffect wrapped)` | " + DONT % "the trigger `Triggers.youPutCountersOnCreatureYouControl()` (Mikey & Leo, Chaos & Order) or `Triggers.youPutPlusOneCountersOnThis()` (Exemplar of Light)" + "The legacy view of those triggers gives it to the collectors of `countersPutOnCreatureYouControl()` / `plusOneCountersPutOnThis()`: it keeps only placements caused by the watcher's controller |"),
        ("| `YouRemoveTimeCounterFromExiledCardTriggerEffect` | `(CardEffect wrapped)` | `Triggers.timeCounterRemovedFromThisInExile()` wrapper that keeps only removals made by the card's controller",
         "| `YouRemoveTimeCounterFromExiledCardTriggerEffect` | `(CardEffect wrapped)` | " + DONT % "the trigger `Triggers.youRemoveTimeCounterFromThisInExile()`" + "The legacy view of that trigger gives it to the `Triggers.timeCounterRemovedFromThisInExile()` collector, which keeps only removals made by the card's controller"),
    ],
    "agent-docs/CARD_PATTERN_INDEX.md": [
        ("`h/HardenedBonds.java` + `Triggers.countersPutOnCreatureYouControl()` + `OncePerTurnTriggerEffect` +",
         "`h/HardenedBonds.java` + `Triggers.plusOneCountersPutOnCreatureYouControl()` + `TriggerLimit.ONCE_EACH_TURN` +"),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`Triggers.plusOneCountersPutOnHumanYouControl() OncePerTurnTriggerEffect(DrawCardEffect(1))`",
         "`Triggers.plusOneCountersPutOnHumanYouControl()` + `%s` + `DrawCardEffect(1)`" % ONCE),
        ("`Triggers.plusOneCountersPutOnCreatureYouControl() from the graveyard` + `MayEffect(ConditionalEffect(SourceCardInGraveyard, PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0)))`",
         "`Triggers.plusOneCountersPutOnCreatureYouControl() from the graveyard` + `TriggerOptions.DEFAULT.withInterveningIf(SourceCardInGraveyard)` + `MayEffect(PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0))`"),
    ],
    "agent-docs/TRIGGER_SLOT_TARGETING.md": [
        ("`Triggers.plusOneCountersPutOnThis()` uses `TriggerMode.PER_COUNTER` for abilities that trigger for each individual counter, such as Bloodcrazed Hoplite.",
         "`Triggers.plusOneCounterPutOnThis()` (\"whenever a +1/+1 counter is put on this\") triggers for each individual counter, such as Bloodcrazed Hoplite; don't author `TriggerMode.PER_COUNTER`."),
        ("that creature's controller; it also accepts a `TriggeringPermanentConditionalEffect` wrapper for\nevent-subject restrictions such as \"another colorless creature\"),",
         "that creature's controller; a restriction on that creature such as \"another colorless creature\" is the\ntrigger's `withFilter(…)`),"),
        ("`Triggers.youPutPlusOneCountersOnCreatureYouControlOnceEachTurn()` (Terrasymbiosis; fires once when the\ncontroller puts one or more +1/+1 counters on a creature they control and carries the number of\ncounters placed as the trigger event value),",
         "`Triggers.youPutPlusOneCountersOnCreatureYouControl()` (Terrasymbiosis; fires once when the\ncontroller puts one or more +1/+1 counters on a creature they control and carries the number of\ncounters placed as the trigger event value; \"do this only once each turn\" is\n`TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`),"),
        ("the controller controls, including counters the creature enters with; a `OncePerTurnTriggerEffect`\nis marked only after its mode condition is met. `OncePerTurnPerCreatureTriggerEffect` uses the same\nslot for first-time-per-creature wording and keys the marker to the watcher and triggering creature),",
         "the controller controls, including counters the creature enters with; its once-each-turn limit\nis marked only after its intervening \"if\" (the chosen mode) is met. `TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE`\nis the first-time-per-creature wording and keys the marker to the watcher and triggering creature;\n`Triggers.youPutCountersOnCreatureYouControl()` counts only the controller's placements),"),
    ],
}


def main():
    dry = "--dry-run" in sys.argv
    for path, edits in EDITS.items():
        src = open(path).read()
        for old, new in edits:
            if src.count(old) != 1:
                sys.exit("%s: expected one match of:\n%s" % (path, old))
            src = src.replace(old, new)
        print(path, len(edits))
        if not dry:
            open(path, "w").write(src)


if __name__ == "__main__":
    main()
