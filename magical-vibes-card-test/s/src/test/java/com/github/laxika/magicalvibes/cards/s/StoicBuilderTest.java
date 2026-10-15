package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BywayCourier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoicBuilder.class, Forest.class, BywayCourier.class})
class StoicBuilderTest extends BaseCardTest {

    private void castStoicBuilder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new StoicBuilder(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted land card from graveyard to hand")
    void etbReturnsLandToHand() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castStoicBuilder();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("A nonland card in the graveyard is not a legal target")
    void nonlandNotTargetable() {
        harness.setGraveyard(player1, List.of(new BywayCourier()));

        castStoicBuilder();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Byway Courier");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castStoicBuilder();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Empty graveyard produces no target choice")
    void emptyGraveyardNoTargetChoice() {
        castStoicBuilder();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("A land target is required even if the return will be declined")
    void landTargetIsRequired() {
        harness.setGraveyard(player1, List.of(new Forest()));

        castStoicBuilder();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lands in an opponent's graveyard cannot be targeted")
    void opponentGraveyardIsNotEligible() {
        harness.setGraveyard(player2, List.of(new Forest()));

        castStoicBuilder();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Only the controller's land cards appear in the target choice")
    void targetChoiceExcludesNonlandsAndOpponentCards() {
        Forest forest = new Forest();
        BywayCourier courier = new BywayCourier();
        Forest opponentForest = new Forest();
        harness.setGraveyard(player1, List.of(forest, courier));
        harness.setGraveyard(player2, List.of(opponentForest));

        castStoicBuilder();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(forest);
    }

    @Test
    @DisplayName("A land removed after targeting is not returned")
    void removedTargetIsNotReturned() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castStoicBuilder();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(forest));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
