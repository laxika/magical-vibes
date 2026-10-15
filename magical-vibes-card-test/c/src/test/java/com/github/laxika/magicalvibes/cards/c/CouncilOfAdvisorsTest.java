package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CouncilOfAdvisors.class, Forest.class})
class CouncilOfAdvisorsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new CouncilOfAdvisors()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("ETB draws exactly one card for its controller, not the other player")
    void opponentControllerDrawsExactlyOneCard() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new CouncilOfAdvisors()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        Forest firstCard = new Forest();
        Forest secondCard = new Forest();
        Forest otherPlayersCard = new Forest();
        harness.setLibrary(player2, List.of(firstCard, secondCard));
        harness.setLibrary(player1, List.of(otherPlayersCard));

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherPlayersCard);
        assertThat(gd.stack).isEmpty();
    }
}
