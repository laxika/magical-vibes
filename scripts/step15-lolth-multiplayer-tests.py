#!/usr/bin/env python3
"""Step 15, follow-up after run 17y4: Lolth, Spider Queen's emblem when creatures you control deal combat damage to two
opponents in the same combat damage step.

The emblem reads "Whenever an opponent is dealt combat damage by one or more creatures you control, if that player lost
less than 8 life this turn, they lose life equal to the difference." It triggers once for each opponent dealt damage,
and each trigger reads only that opponent's life lost (added with the user).

- `LolthSpiderQueenTest#emblemTriggersForEachDamagedOpponent`

Idempotent: a test already holding its case is left alone. Per letter test module.

Usage: step15-lolth-multiplayer-tests.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PATH = ROOT / "magical-vibes-card-test/l/src/test/java/com/github/laxika/magicalvibes/cards/l/LolthSpiderQueenTest.java"
METHOD = "emblemTriggersForEachDamagedOpponent"
ANCHOR = "    private Player addThirdPlayer() {"
CASE = """    @Test
    @DisplayName("With two opponents dealt combat damage at once the emblem triggers for each of them")
    void emblemTriggersForEachDamagedOpponent() {
        Player player3 = addThirdPlayer();
        addReadyLolth(player1, 8);
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        firstBear.setAttacking(true);
        firstBear.setAttackTarget(player2.getId());
        secondBear.setAttacking(true);
        secondBear.setAttackTarget(player3.getId());
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.getLife(player3.getId())).isEqualTo(12);
    }

"""


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    if "l" not in letters:
        return
    text = PATH.read_text()
    if f"void {METHOD}()" in text:
        return
    if text.count(ANCHOR) != 1:
        raise SystemExit("LolthSpiderQueenTest: addThirdPlayer() anchor not found")
    PATH.write_text(text.replace(ANCHOR, CASE + ANCHOR, 1))
    print(f"added LolthSpiderQueenTest#{METHOD}")


if __name__ == "__main__":
    main()
