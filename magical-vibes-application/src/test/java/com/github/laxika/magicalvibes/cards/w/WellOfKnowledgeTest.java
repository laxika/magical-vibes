package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WellOfKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller pays {2} to draw a card during their draw step")
    void controllerDrawsDuringOwnDrawStep() {
        addWell(player1);
        advanceToDraw(player1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    @DisplayName("Any player may activate Well of Knowledge during their own draw step")
    void opponentDrawsDuringOwnDrawStep() {
        addWell(player1);
        advanceToDraw(player2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    @DisplayName("A player cannot activate Well of Knowledge during another player's draw step")
    void opponentCannotActivateDuringControllersDrawStep() {
        addWell(player1);
        advanceToDraw(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("draw step");
    }

    @Test
    @DisplayName("Well of Knowledge cannot be activated outside a draw step")
    void cannotActivateOutsideDrawStep() {
        addWell(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("draw step");
    }

    private void addWell(Player owner) {
        harness.addToBattlefield(owner, new WellOfKnowledge());
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.setLibrary(activePlayer, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(activePlayer == player1 ? player2 : player1,
                List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
    }
}
