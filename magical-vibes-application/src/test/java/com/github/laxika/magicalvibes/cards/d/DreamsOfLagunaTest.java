package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamsOfLaguna.class, GrizzlyBears.class})
class DreamsOfLagunaTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the surveilled card into the graveyard before drawing")
    void surveilsThenDraws() {
        Card surveilledCard = new GrizzlyBears();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(surveilledCard, drawnCard));
        harness.setHand(player1, List.of(new DreamsOfLaguna()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveilledCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Draws the top card when surveil is declined")
    void declinedSurveilDrawsTopCard() {
        Card topCard = new GrizzlyBears();
        Card remainingCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.setHand(player1, List.of(new DreamsOfLaguna()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("Flashback resolves the spell and exiles it")
    void flashbackResolvesAndExiles() {
        Card flashbackCard = new DreamsOfLaguna();
        Card surveilledCard = new GrizzlyBears();
        Card drawnCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(flashbackCard));
        harness.setLibrary(player1, List.of(surveilledCard, drawnCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(flashbackCard);
    }
}
