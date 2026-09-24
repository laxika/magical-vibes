package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ObekaBruteChronologist.class)
class ObekaBruteChronologistTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may end the turn")
    void activePlayerMayEndTheTurn() {
        addReadyObeka(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int turnBefore = gd.turnNumber;
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The active player may decline to end the turn")
    void activePlayerMayDecline() {
        addReadyObeka(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int turnBefore = gd.turnNumber;
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyObeka(Player player) {
        Permanent obeka = harness.addToBattlefieldAndReturn(player, new ObekaBruteChronologist());
        obeka.setSummoningSick(false);
        return obeka;
    }
}
