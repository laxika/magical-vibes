package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoctorDoomUnrivaled.class, GrizzlyBears.class})
class DoctorDoomUnrivaledTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life while cards remain in the library")
    void drawsAndLosesLifeWithoutWinning() {
        addReadyDoom(player1);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        activateDoom();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Wins after drawing the last card from the library")
    void winsWhenDrawEmptiesLibrary() {
        addReadyDoom(player1);
        Card lastCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(lastCard));

        activateDoom();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Wins even when the activated ability draws from an empty library")
    void winsWithEmptyLibrary() {
        addReadyDoom(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        activateDoom();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Wins at zero life after drawing the last card")
    void winsAtZeroLife() {
        addReadyDoom(player1);
        Card lastCard = new DoctorDoomUnrivaled();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(lastCard));
        harness.setLife(player1, 1);

        activateDoom();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Wins at zero life even when no card could be drawn")
    void winsAtZeroLifeWithEmptyLibrary() {
        addReadyDoom(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 1);

        activateDoom();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Ability still wins after Doom leaves the battlefield")
    void winsAfterSourceLeavesBattlefield() {
        Permanent doom = addReadyDoom(player1);
        Card lastCard = new DoctorDoomUnrivaled();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(lastCard));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(doom);
        gd.playerGraveyards.get(player1.getId()).add(doom.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lastCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private void activateDoom() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyDoom(Player player) {
        Permanent doom = addCreatureReady(player, new DoctorDoomUnrivaled());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return doom;
    }
}
