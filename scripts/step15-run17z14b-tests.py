#!/usr/bin/env python3
"""Step 15, run 17z14b: the card test of the rules fix (row 17z14b).

Scouring Swarm's one Oracle ability, written as two `addTrigger` calls the sacrifice collector put on the stack one
effect at a time, is one `addTrigger` (`step15-run17z14b-cards.py`) that the collector dispatches whole: one stack
object (CR 603.3), not two an opponent could respond between. Fails if the edit didn't apply; idempotent.

Usage: step15-run17z14b-tests.py [REPOSITORY_ROOT]
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent)
TEST = ROOT / "magical-vibes-card-test/s/src/test/java/com/github/laxika/magicalvibes/cards/s/ScouringSwarmTest.java"

ANCHOR = """    @Test
    @DisplayName("Seven land cards make the trigger create a tapped token copy")"""
CASE = """    @Test
    @DisplayName("Sacrificing a land puts one ability on the stack, not one for each token clause (CR 603.3)")
    void sacrificeAbilityIsOneStackObject() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new ScouringSwarm());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack.stream().filter(entry -> entry.getCard().getName().equals("Scouring Swarm")))
                .singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

"""

text = TEST.read_text()
if "sacrificeAbilityIsOneStackObject" not in text:
    if ANCHOR not in text:
        raise SystemExit("ScouringSwarmTest: anchor not found")
    TEST.write_text(text.replace(ANCHOR, CASE + ANCHOR, 1))
    print("added ScouringSwarmTest.sacrificeAbilityIsOneStackObject")
