#!/usr/bin/env python3
"""Step 15 run 17z11b: the card-test cases for the damage-to-a-player event recorded on the entry.

Idempotent; optional repository-root argument (used for the HEAD worktree).

- BlizzardSpecterTest#discardModeIgnoresHexproof: "that player" of a combat damage modal is not a target, so a
  damaged player with hexproof still discards (the modal's entry was not non-targeting).
- InvocationOfSaintTraftTest#angelTokenExiledAtEndOfCombat: the granted ability's token creation records its delayed
  exile with the ability's source card and controller, so the case matches the action by token and kind.
"""
import os
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
T = os.path.join(ROOT, 'magical-vibes-card-test')


def test_path(letter, name):
    return os.path.join(T, letter, 'src/test/java/com/github/laxika/magicalvibes/cards', letter, name + '.java')


def add_case(letter, name, marker, case, imports=()):
    p = test_path(letter, name)
    with open(p) as f:
        src = f.read()
    if marker in src:
        return
    for imp in imports:
        line = 'import ' + imp + ';\n'
        if line not in src:
            i = src.index('import ')
            src = src[:i] + line + src[i:]
    end = src.rstrip().rindex('}')
    src = src[:end].rstrip() + '\n\n' + case + '}\n'
    with open(p, 'w') as f:
        f.write(src)
    print('added', name, marker)


add_case('b', 'BlizzardSpecterTest', 'discardModeIgnoresHexproof', '''    @Test
    @CardUsed(LeylineOfSanctity.class)
    @DisplayName("That player isn't targeted: a damaged player with hexproof still discards")
    void discardModeIgnoresHexproof() {
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setHand(player2, List.of(new BorealDruid()));
        Permanent specter = addCreatureReady(player1, new BlizzardSpecter());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Boreal Druid");
    }
''', imports=['com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity'])


def replace_in_test(letter, name, old, new):
    p = test_path(letter, name)
    with open(p) as f:
        src = f.read()
    if new in src:
        return
    if old not in src:
        sys.exit(f'{name}: expected text not found')
    with open(p, 'w') as f:
        f.write(src.replace(old, new))
    print('changed', name)


replace_in_test('i', 'InvocationOfSaintTraftTest', """        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(
                        angel.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));""",
                """        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(angel.getId())
                        && action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT);""")
