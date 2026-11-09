#!/usr/bin/env python3
"""Step 15, run 17z11c2: Unstoppable Ash's entry assertion.

Idempotent; optional repository-root argument (used for the HEAD worktree). The "a creature you control becomes
blocked" ability's source is the watcher (Unstoppable Ash, CR 113.7) and the blocked creature its triggering
permanent; the case asserted the source the collector rebound to the blocked creature.
"""
import pathlib
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
test = root / 'magical-vibes-card-test/u/src/test/java/com/github/laxika/magicalvibes/cards/u/UnstoppableAshTest.java'
text = test.read_text()
old_add = """        bears.setAttacking(true);
        addCreatureReady(player1, new UnstoppableAsh());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);"""
new_add = """        bears.setAttacking(true);
        Permanent ash = addCreatureReady(player1, new UnstoppableAsh());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);"""
old_assert = "        assertThat(trigger.getSourcePermanentId()).isEqualTo(bears.getId());\n"
new_assert = ("        assertThat(trigger.getSourcePermanentId()).isEqualTo(ash.getId());\n"
              "        assertThat(trigger.getTriggeringPermanentId()).isEqualTo(bears.getId());\n")
if new_assert not in text:
    if old_add not in text or old_assert not in text:
        sys.exit('UnstoppableAshTest: expected text not found')
    text = text.replace(old_add, new_add, 1).replace(old_assert, new_assert, 1)
    test.write_text(text)
    print('UnstoppableAshTest updated')
