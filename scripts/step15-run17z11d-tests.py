#!/usr/bin/env python3
"""Step 15, run 17z11d: the new Pip-Boy 3000 case.

Idempotent; optional repository-root argument (used for the HEAD worktree). "Whenever equipped creature attacks" is the
Equipment's own ability, so it triggers for an equipped creature that lost all its abilities (Humble); written as an
ability granted to the creature it didn't.
"""
import pathlib
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
test = root / 'magical-vibes-card-test/p/src/test/java/com/github/laxika/magicalvibes/cards/p/PipBoy3000Test.java'
text = test.read_text()
if 'equipmentsOwnAbilityTriggersForACreatureThatLostItsAbilities' not in text:
    text = text.replace('import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n',
                        'import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;\n'
                        'import com.github.laxika.magicalvibes.cards.h.Humble;\n', 1)
    text = text.replace('@CardUsed({PipBoy3000.class, Forest.class, GrizzlyBears.class})',
                        '@CardUsed({PipBoy3000.class, Forest.class, GrizzlyBears.class, Humble.class})', 1)
    case = """    @Test
    void equipmentsOwnAbilityTriggersForACreatureThatLostItsAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equip(creature);
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        declareAttackers(List.of(0));
        harness.handleListChoice(player1, PICK_A_PERK);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void equip(Permanent creature) {"""
    if '    private void equip(Permanent creature) {' not in text:
        sys.exit('PipBoy3000Test: expected text not found')
    text = text.replace('    private void equip(Permanent creature) {', case, 1)
    test.write_text(text)
    print('PipBoy3000Test updated')
