package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Archaeomancer.class, Negate.class, Divination.class})
class ArchaeomancerTest extends BaseCardTest {

    /** Casts Archaeomancer and resolves it so its ETB sets up graveyard targeting. */
    private void castArchaeomancer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Archaeomancer()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted instant card from graveyard to hand")
    void etbReturnsInstantToHand() {
        Negate negate = new Negate();
        harness.setGraveyard(player1, List.of(negate));

        castArchaeomancer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(negate.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Negate");
        harness.assertNotInGraveyard(player1, "Negate");
    }

    @Test
    @DisplayName("ETB returns a targeted sorcery card from graveyard to hand")
    void etbReturnsSorceryToHand() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));

        castArchaeomancer();

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("A creature card in the graveyard is not a legal target")
    void creatureNotTargetable() {
        harness.setGraveyard(player1, List.of(new Archaeomancer()));

        castArchaeomancer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Archaeomancer");
    }

    @Test
    @DisplayName("Only the selected card is returned when multiple cards qualify")
    void returnsOnlySelectedCard() {
        Negate negate = new Negate();
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(negate, divination, new Archaeomancer()));

        castArchaeomancer();
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Negate");
        harness.assertInGraveyard(player1, "Archaeomancer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's instant or sorcery cannot supply the target")
    void opponentGraveyardNotTargetable() {
        harness.setGraveyard(player2, List.of(new Negate(), new Divination()));

        castArchaeomancer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Negate");
        harness.assertInGraveyard(player2, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not replaced by another eligible card")
    void missingTargetDoesNotReturnAnotherCard() {
        Negate negate = new Negate();
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(negate, divination));

        castArchaeomancer();
        harness.handleMultipleCardsChosen(player1, List.of(negate.getId()));
        harness.setGraveyard(player1, List.of(divination));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Negate");
        harness.assertNotInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Empty graveyard leaves no target choice")
    void emptyGraveyardNoTargetChoice() {
        castArchaeomancer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
