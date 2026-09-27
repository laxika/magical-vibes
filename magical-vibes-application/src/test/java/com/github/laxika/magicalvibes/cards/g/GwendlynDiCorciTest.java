package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GwendlynDiCorci.class)
class GwendlynDiCorciTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card at random")
    void targetPlayerDiscardsAtRandom() {
        Permanent gwendlyn = setupGwendlynOnMyTurn();
        harness.setHand(player2, List.of(new GwendlynDiCorci()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Gwendlyn Di Corci");
        assertThat(gwendlyn.isTapped()).isTrue();
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("discards") && log.contains("at random"));
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        Permanent gwendlyn = setupGwendlynOnMyTurn();
        harness.setHand(player1, List.of(new GwendlynDiCorci()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Gwendlyn Di Corci");
        assertThat(gwendlyn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new GwendlynDiCorci());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        setupGwendlynOnMyTurn();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GwendlynDiCorci());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent setupGwendlynOnMyTurn() {
        Permanent gwendlyn = addCreatureReady(player1, new GwendlynDiCorci());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gwendlyn;
    }
}
