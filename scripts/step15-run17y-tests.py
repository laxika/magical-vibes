#!/usr/bin/env python3
"""Step 15, run 17y: the card test of the rules fix.

The "deals damage to a creature" abilities the collector built from trigger-condition records went straight onto the
stack, outside the collector's dispatch, so "it triggers an additional time" (Roaming Throne) never applied to them.
They now go through it, as every other triggered ability does: Bellowing Fiend with Roaming Throne naming Spirit
triggers twice.

Idempotent: a test already added is left alone. Optional repository-root argument (for the HEAD worktree).

Usage: step15-run17y-tests.py [<repository root>]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
TEST = ROOT / "magical-vibes-card-test/b/src/test/java/com/github/laxika/magicalvibes/cards/b/BellowingFiendTest.java"

METHOD = '''    @Test
    @CardUsed({BellowingFiend.class, RoamingThrone.class, WindDrake.class})
    @DisplayName("Roaming Throne naming Spirit makes its ability trigger an additional time")
    void triggersAnAdditionalTimeWithRoamingThrone() {
        Permanent fiend = addCreatureReady(player1, new BellowingFiend());
        fiend.setAttacking(true);
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new RoamingThrone());
        throne.setChosenSubtype(CardSubtype.SPIRIT);
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);

        blockAttacker(player2, new WindDrake(), 0);

        resolveCombat();
        resolveAllTriggers();

        // Each of the two instances deals 3 damage to the blocker's controller and 3 to Bellowing Fiend's controller.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

'''
ANCHOR = '''    @Test
    @DisplayName("Does not trigger when it deals combat damage to a player")'''
IMPORTS = ["import com.github.laxika.magicalvibes.cards.r.RoamingThrone;",
           "import com.github.laxika.magicalvibes.model.CardSubtype;"]


def main():
    text = TEST.read_text()
    if "triggersAnAdditionalTimeWithRoamingThrone" in text:
        return
    if ANCHOR not in text:
        raise SystemExit("BellowingFiendTest: anchor not found")
    text = text.replace(ANCHOR, METHOD + ANCHOR, 1)
    for line in IMPORTS:
        if line not in text:
            anchor = ("import com.github.laxika.magicalvibes.model.Card;" if "model" in line
                      else "import com.github.laxika.magicalvibes.cards.p.PreyUpon;")
            text = text.replace(anchor, anchor + "\n" + line, 1)
    TEST.write_text(text)
    print("added BellowingFiendTest#triggersAnAdditionalTimeWithRoamingThrone")


if __name__ == "__main__":
    main()
