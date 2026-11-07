#!/usr/bin/env python3
"""Step 15, run 17i: card tests that asserted an attack trigger's event condition as an intervening "if".

Kari Zev, Crew of Two: "Whenever Kari Zev attacks while you don't control a legendary Monkey" - "while" is part of
the trigger condition, not an "if" that immediately follows it (CR 603.4), so a legendary Monkey that arrives before the
ability resolves doesn't stop the conjuring. Idempotent.

Usage: step15-run17i-tests.py <card-test letter module dir>...
"""
import pathlib
import sys

ROOT = "src/test/java/com/github/laxika/magicalvibes/cards"
EDITS = {
    "k/KariZevCrewOfTwoTest.java": [(
        """    @Test
    @DisplayName("The legendary Monkey condition is checked again when the trigger resolves")
    void conditionIsCheckedAtResolution() {
        addCreatureReady(player1, new KariZevCrewOfTwo());

        declareAttackers(List.of(0));
        addCreatureReady(player1, new RagavanNimblePilferer());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ragavan, Nimble Pilferer")).hasSize(1);
    }""",
        """    @Test
    @DisplayName("A legendary Monkey that arrives before the trigger resolves doesn't stop the conjuring")
    void conditionIsOnlyCheckedAsTheAbilityTriggers() {
        addCreatureReady(player1, new KariZevCrewOfTwo());

        declareAttackers(List.of(0));
        addCreatureReady(player1, new RagavanNimblePilferer());
        harness.passBothPriorities();

        // "Attacks while you don't control a legendary Monkey" is the trigger event (CR 603.4 checks only an "if"
        // that follows the trigger condition), so the conjured Ragavan joins the one that arrived.
        assertThat(findPermanents(player1, "Ragavan, Nimble Pilferer")).hasSize(2)
                .anySatisfy(ragavan -> assertThat(ragavan.isAttacking()).isTrue());
    }""")],
}


def main(modules):
    for module in modules:
        letter = pathlib.Path(module).name
        for relative, edits in EDITS.items():
            if not relative.startswith(letter + "/"):
                continue
            path = pathlib.Path(module) / ROOT / relative
            source = path.read_text()
            for old, new in edits:
                if new in source:
                    continue
                if source.count(old) != 1:
                    raise SystemExit(f"{relative}: old case not found")
                source = source.replace(old, new)
            path.write_text(source)
            print(f"edited {relative}")


if __name__ == "__main__":
    main(sys.argv[1:])
