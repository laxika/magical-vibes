package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JaceWielderOfMysteries.class, GrizzlyBears.class})
class JaceWielderOfMysteriesTest extends BaseCardTest {

    @Test
    @DisplayName("+1 mills two cards from the target player and draws a card")
    void plusOneMillsAndDraws() {
        Permanent jace = addReadyJace(player1, 4);
        stockLibrary(player1, 1);
        stockLibrary(player2, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 wins instead of losing when its controller draws from an empty library")
    void plusOneWinsOnEmptyLibraryDraw() {
        addReadyJace(player1, 4);
        stockLibrary(player1, 0);
        stockLibrary(player2, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("-8 draws seven cards and wins when that empties the library")
    void minusEightWinsWithEmptyLibraryAfterDrawing() {
        Permanent jace = addReadyJace(player1, 8);
        stockLibrary(player1, 7);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-8 does not win when cards remain in the library")
    void minusEightDoesNotWinWithCardsRemaining() {
        Permanent jace = addReadyJace(player1, 8);
        stockLibrary(player1, 8);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private void stockLibrary(Player player, int count) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            library.add(new GrizzlyBears());
        }
        harness.setLibrary(player, library);
        harness.setHand(player, List.of());
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent jace = new Permanent(new JaceWielderOfMysteries());
        jace.setCounterCount(CounterType.LOYALTY, loyalty);
        jace.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jace);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jace;
    }
}
