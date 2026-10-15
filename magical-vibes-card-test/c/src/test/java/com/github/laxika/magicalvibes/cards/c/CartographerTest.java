package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cartographer.class, CityOfTraitors.class})
class CartographerTest extends BaseCardTest {

    /** Casts Cartographer and resolves the creature spell so its ETB trigger sets up graveyard targeting. */
    private void castCartographer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Cartographer(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted land card from graveyard to hand")
    void etbReturnsLandToHand() {
        CityOfTraitors land = new CityOfTraitors();
        harness.setGraveyard(player1, List.of(land));

        castCartographer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "City of Traitors");
        harness.assertNotInGraveyard(player1, "City of Traitors");
    }

    @Test
    @DisplayName("A nonland card in the graveyard is not a legal target")
    void nonlandNotTargetable() {
        harness.setGraveyard(player1, List.of(new Cartographer()));

        castCartographer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Cartographer");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        CityOfTraitors land = new CityOfTraitors();
        harness.setGraveyard(player1, List.of(land));

        castCartographer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "City of Traitors");
        harness.assertNotInHand(player1, "City of Traitors");
    }

    @Test
    @DisplayName("A legal land target must be chosen even when the return will be declined")
    void legalTargetIsRequiredBeforeOptionalReturn() {
        CityOfTraitors land = new CityOfTraitors();
        Cartographer nonland = new Cartographer();
        CityOfTraitors opponentLand = new CityOfTraitors();
        harness.setGraveyard(player1, List.of(land, nonland));
        harness.setGraveyard(player2, List.of(opponentLand));

        castCartographer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(land);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Empty graveyard produces no target choice")
    void emptyGraveyardNoTargetChoice() {
        castCartographer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("A land that leaves the graveyard before resolution is not returned")
    void targetMustStillBeInGraveyardAtResolution() {
        CityOfTraitors land = new CityOfTraitors();
        harness.setGraveyard(player1, List.of(land));

        castCartographer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(land));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A land card in an opponent's graveyard is not a legal target")
    void opponentLandNotTargetable() {
        harness.setGraveyard(player2, List.of(new CityOfTraitors()));

        castCartographer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "City of Traitors");
    }
}
