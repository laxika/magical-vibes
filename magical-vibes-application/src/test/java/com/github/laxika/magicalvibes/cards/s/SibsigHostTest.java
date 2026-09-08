package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SibsigHostTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from each player's library")
    void etbMillsThreeCardsFromEachPlayer() {
        int playerDeckSize = gd.playerDecks.get(player1.getId()).size();
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new SibsigHost()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(playerDeckSize - 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
