#!/usr/bin/env python3
"""Step 15, run 17m: the card test of the run's rules fix.

- Aggression: "destroy that creature if it didn't attack this turn" is an effect "if" (CR 603.4: an intervening "if"
  only follows the trigger condition), so the ability goes on the stack at its controller's end step even when the
  creature attacked, and does nothing as it resolves; the collector kept it off the stack by the condition's class.
  Insubordination's "deals 2 damage ... if it didn't attack" takes the same path.

Idempotent.

Usage: step15-run17m-tests.py <letter test module dir>...
"""
import pathlib
import sys

CASE = '''
    @Test
    @DisplayName("The ability goes on the stack for a creature that attacked and does nothing as it resolves")
    void triggersForAnAttackerAndDoesNothing() {
        Permanent bears = addCreature(player1);
        attach(player1, bears);
        bears.setAttackedThisTurn(true);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard().getName()).isEqualTo("Aggression"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }
'''


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/test/java/**/AggressionTest.java")):
            source = path.read_text()
            if "void triggersForAnAttackerAndDoesNothing()" in source:
                continue
            end = source.rstrip().rindex("}")
            path.write_text(source[:end].rstrip() + "\n" + CASE + "}\n")
            changed.append(path.stem)
    print(f"{len(changed)} test classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
