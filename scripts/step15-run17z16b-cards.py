#!/usr/bin/env python3
"""Step 15, run 17z16b: Vodalian Mindsinger's target restriction is the one it prints.

Oracle text checked with the Scryfall MCP:

- Vodalian Mindsinger (DMU 76): "Kicker {1}{R} and/or {1}{G}. This creature enters with two +1/+1 counters on it for
  each time it was kicked. When this creature enters, gain control of target creature with power less than this
  creature's power for as long as you control this creature." The card also gave its target filter a "kicked"
  restriction of power 3 or less, which is right for one kicker and wrong for both (power 6, a target of power 5 or
  less). It was never read as the ability resolved because the single-target enters route didn't record that the
  permanent was kicked; with the trigger record carried on every route it is, and a creature of power 4 became an
  illegal target of the twice-kicked ability. The printed restriction reads the creature's power as the target is
  chosen and as the ability resolves, so the kicked one goes.

  Two of its tests named the target as the kicked spell was cast, which the test harness checks against the filter
  before the creature exists (the kicked restriction stood in for it there); they choose the target as the ability
  goes on the stack (CR 603.3d), as `bothKickersAddFourCountersAndAllowStealingFourPowerCreature` does.

Idempotent; fails if an edit doesn't apply.

Usage: python3 scripts/step15-run17z16b-cards.py [LETTER...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = "magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
TESTS = ("magical-vibes-card-test/{letter}/src/test/java/com/github/laxika/magicalvibes/cards/{letter}/"
         "{name}.java")

EDITS = {
    "VodalianMindsinger": [
        ("""                "Target must be a creature with power less than Vodalian Mindsinger's power",
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtMostPredicate(3)
                ))
        );
""",
         """                "Target must be a creature with power less than Vodalian Mindsinger's power"
        );
"""),
        ("import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;\n", ""),
    ],
    "VodalianMindsingerTest": [
        ("""        harness.castKickedCreature(player1, 0, target.getId());
        resolveAllTriggers();

        Permanent mindsinger = findPermanent(player1, "Vodalian Mindsinger");
        assertThat(mindsinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
""",
         """        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        Permanent mindsinger = findPermanent(player1, "Vodalian Mindsinger");
        assertThat(mindsinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
"""),
        ("""        harness.castKickedCreature(player2, 0, source.getId());
        resolveAllTriggers();
""",
         """        harness.castKickedCreature(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, source.getId());
        resolveAllTriggers();
"""),
    ],
}


def main():
    letters = set(sys.argv[1:])
    for name, edits in sorted(EDITS.items()):
        letter = name[0].lower()
        if letters and letter not in letters:
            continue
        path = ROOT / (TESTS if name.endswith("Test") else CARDS).format(letter=letter, name=name)
        text = path.read_text()
        changed = False
        for old, new in edits:
            if old in text:
                text = text.replace(old, new)
                changed = True
            elif new and new in text:
                continue
            elif not new:
                continue
            else:
                sys.exit(f"{name}: edit doesn't apply: {old[:60]!r}")
        if changed:
            path.write_text(text)
            print("edited", name)
        else:
            print("unchanged", name)


if __name__ == "__main__":
    main()
