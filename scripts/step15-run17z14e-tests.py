#!/usr/bin/env python3
"""Step 15, run 17z14e: the card tests the run adds to or changes.

`CASES`: Tenured Oilcaster's "whenever this creature attacks or blocks, each player mills a card" is one ability, so
one stack object (CR 603.3); written as two `addTrigger` calls it went on the stack as two objects when the creature
attacked, an opponent able to respond between the two mills. The case fails at `d52ab33db2` and passes after run
17z14e.

`EDITS`: Sovereigns of Lost Alara has exalted and "whenever a creature you control attacks alone, you may search ...",
two abilities that trigger together. The collector put the "you may" on the stack first, under exalted, so exalted
had resolved by the time the "you may" was asked; it now puts a permanent's abilities on the stack in the order they
are written, so the "you may" is asked while exalted is still on the stack (their controller may order them either
way, CR 603.3b). Five cases asserted the attacker's power right after answering the "you may"; they resolve the rest
of the stack first. No expected value changes. Putrefying Rotboar's case is a fix of a test that fails at
`d52ab33db2` already (see its comment).

Usage: step15-run17z14e-tests.py [REPOSITORY_ROOT]. Fails if an edit didn't apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TESTS = ROOT / "magical-vibes-card-test"
PKG = "src/test/java/com/github/laxika/magicalvibes/cards"

# test class -> (anchor the case goes before, method signature, case)
CASES = {
    "TenuredOilcasterTest": (
        "    @Test\n    @DisplayName(\"Blocking makes each player mill a card\")",
        "void attackAbilityIsOneStackObject()",
        """    @Test
    @DisplayName("The attack ability is one stack object")
    void attackAbilityIsOneStackObject() {
        addCreatureReady(player1, new TenuredOilcaster());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
    }

"""),
}


# test class -> [(old, new, occurrences)]
EDITS = {
    "SovereignsOfLostAlaraTest": [
        ("""        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

""",
         """        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        resolveAllTriggers();

""", 2),
        ("""        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

""",
         """        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        resolveAllTriggers();

""", 1),
        ("""        harness.handleMayAbilityChosen(player1, false);

""",
         """        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

""", 1),
        ("""        declareAttackers(player1, List.of(1));
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

""",
         """        declareAttackers(player1, List.of(1));
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

""", 1),
    ],
    # Not this run's: the case fails at `d52ab33db2` too. Two attacking Boars give the card four "when you cast this
    # spell, you lose 1 life" abilities, four stack objects since run 17z11f2 (CR 603.3b); the case passed priority
    # once and asserted all four had resolved.
    "PutrefyingRotboarTest": [
        ("""        castHandCreature(player2);

        harness.assertLife(player2, 16);
""",
         """        castHandCreature(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
""", 1),
    ],
}


def test_path(name):
    letter = name[0].lower()
    return TESTS / letter / PKG / letter / f"{name}.java"


def main():
    failed = []
    for name, (anchor, signature, case) in CASES.items():
        path = test_path(name)
        text = path.read_text()
        if signature in text:
            continue
        if text.count(anchor) != 1:
            failed.append(name)
            continue
        path.write_text(text.replace(anchor, case + anchor))
    for name, edits in EDITS.items():
        path = test_path(name)
        text = path.read_text()
        for old, new, occurrences in edits:
            if text.count(new) == occurrences:
                continue
            if text.count(old) != occurrences:
                failed.append(name)
                break
            text = text.replace(old, new)
        else:
            path.write_text(text)
    if failed:
        sys.exit("Edits not applied: " + ", ".join(failed))


if __name__ == "__main__":
    main()
