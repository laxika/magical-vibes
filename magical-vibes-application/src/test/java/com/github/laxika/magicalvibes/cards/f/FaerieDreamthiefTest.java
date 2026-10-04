package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaerieDreamthief.class})
class FaerieDreamthiefTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1 and can put the top card into the graveyard")
    void entersWithSurveilOneAccepted() {
        FaerieDreamthief dreamthief = new FaerieDreamthief();
        Card topCard = new FaerieDreamthief();
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
        Card topCard = new FaerieDreamthief();
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
        Card cardToDraw = new FaerieDreamthief();
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

    @Test
    @DisplayName("Surveilling an empty library completes without drawing or losing life")
    void entersWithEmptyLibrary() {
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FaerieDreamthief()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Faerie Dreamthief");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated with only two mana")
    void graveyardAbilityRequiresThreeMana() {
        FaerieDreamthief dreamthief = new FaerieDreamthief();
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(dreamthief));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(dreamthief);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(dreamthief);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The graveyard ability requires black mana")
    void graveyardAbilityRequiresBlackMana() {
        FaerieDreamthief dreamthief = new FaerieDreamthief();
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(dreamthief));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(dreamthief);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(dreamthief);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The graveyard ability can be activated during the opponent's turn and affects its controller")
    void graveyardAbilityOnOpponentsTurn() {
        FaerieDreamthief dreamthief = new FaerieDreamthief();
        Card cardToDraw = new FaerieDreamthief();
        harness.forceActivePlayer(player2);
        harness.setGraveyard(player1, List.of(dreamthief));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(cardToDraw));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        List<Card> opponentHandBefore = List.copyOf(gd.playerHands.get(player2.getId()));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreamthief);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToDraw);
        harness.assertLife(player1, lifeBefore - 1);
        harness.assertLife(player2, opponentLifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHandBefore);
    }
}
