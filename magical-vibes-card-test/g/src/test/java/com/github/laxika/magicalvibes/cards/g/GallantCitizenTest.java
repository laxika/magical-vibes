package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GallantCitizen.class, Forest.class})
class GallantCitizenTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void etbDrawsACard() {
        harness.setHand(player1, List.of(new GallantCitizen()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The draw waits for the enter trigger to resolve and draws exactly one card")
    void drawWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new GallantCitizen()));
        Forest topCard = new Forest();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gallant Citizen");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Entering without being cast draws for the creature's controller")
    void enteringWithoutCastingDrawsForController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Forest drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player2, new GallantCitizen());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
