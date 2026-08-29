package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WailingGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills two cards from its controller's library")
    void etbMillsTwoCards() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest()));

        harness.setHand(player1, List.of(new WailingGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB mills its controller, not an opponent")
    void etbMillsControllerNotOpponent() {
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        harness.setHand(player1, List.of(new WailingGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardSize);
    }

    @Test
    @DisplayName("ETB mills only the cards remaining in a short library")
    void etbMillsOnlyRemainingCards() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.setHand(player1, List.of(new WailingGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
