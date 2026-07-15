package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoggartForagerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating puts a player-targeting ability on the stack and sacrifices the creature")
    void activatePutsAbilityOnStack() {
        harness.addToBattlefield(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());

        // Boggart Forager is sacrificed as a cost
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Boggart Forager"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Boggart Forager"));

        // Ability is on the stack targeting the chosen player
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving shuffles the targeted controller's library")
    void resolvingShufflesOwnLibrary() {
        harness.addToBattlefield(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        // Shuffle does not change library size
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Can target an opponent to shuffle their library")
    void canTargetOpponent() {
        harness.addToBattlefield(player1, new BoggartForager());
        harness.addMana(player1, ManaColor.RED, 1);

        int opponentDeckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSizeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Cannot activate without the required mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new BoggartForager());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
