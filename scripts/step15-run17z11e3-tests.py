#!/usr/bin/env python3
"""Step 15, run 17z11e3: Giant Oyster's new case (CR 603.7a: the counter-removal delayed trigger is created even when
the Oyster untapped before its ability resolved). Idempotent; optional repository-root argument (the HEAD worktree)."""
import pathlib
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
path = root / 'magical-vibes-card-test/g/src/test/java/com/github/laxika/magicalvibes/cards/g/GiantOysterTest.java'

NAME = 'oysterUntappedBeforeResolutionStillRemovesCountersWhenItNextUntaps'
CASE = '''    @Test
    @DisplayName("An Oyster untapped before its ability resolves removes the counters when it next untaps")
    void oysterUntappedBeforeResolutionStillRemovesCountersWhenItNextUntaps() {
        Permanent oyster = addOyster(player1);
        Permanent creature = addMinotaur(player2, true);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.inMutationScope(oyster::untap);
        harness.passBothPriorities();

        advanceToDraw(player1);
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        oyster.tap();
        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isFalse();
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(oyster.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

'''
ANCHOR = '''    @Test
    @DisplayName("Giant Oyster leaving the battlefield removes all -1/-1 counters from the locked creature")'''

text = path.read_text()
if NAME in text:
    print('unchanged', path)
else:
    assert text.count(ANCHOR) == 1
    path.write_text(text.replace(ANCHOR, CASE + ANCHOR))
    print('added', NAME, 'to', path)
