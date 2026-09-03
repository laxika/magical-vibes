package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieDreamthief.class, GrizzlyBears.class})
class FaerieDreamthiefTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1 and can put the top card into the graveyard")
    void entersWithSurveilOneAccepted() {
        FaerieDreamthief dreamthief = new FaerieDreamthief();
        Card topCard = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(dreamthief));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Entering the battlefield surveils 1 and can leave the top card on the library")
    void entersWithSurveilOneDeclined() {
        FaerieDreamthief dreamthief = new FaerieDreamthief();
        Card topCard = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(dreamthief));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("The graveyard ability exiles the source, draws a card, and loses 1 life")
    void graveyardAbilityExilesDrawsAndLosesLife() {
        FaerieDreamthief dreamthief = new FaerieDreamthief();
        Card cardToDraw = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(dreamthief));
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(dreamthief);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreamthief);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(cardToDraw);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}
