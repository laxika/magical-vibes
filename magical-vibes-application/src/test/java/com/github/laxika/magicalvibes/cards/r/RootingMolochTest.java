package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrannithHealer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootingMoloch.class, DrannithHealer.class, GrizzlyBears.class})
class RootingMolochTest extends BaseCardTest {

    @Test
    @DisplayName("ETB only targets a cycling card from your graveyard")
    void etbOnlyTargetsOwnCyclingCard() {
        Card cyclingCard = new DrannithHealer();
        Card nonCyclingCard = new GrizzlyBears();
        Card opponentCyclingCard = new DrannithHealer();
        harness.setGraveyard(player1, List.of(cyclingCard, nonCyclingCard));
        harness.setGraveyard(player2, List.of(opponentCyclingCard));

        castRootingMoloch();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(cyclingCard.getId());
    }

    @Test
    @DisplayName("ETB exiles the chosen cycling card and grants play permission")
    void etbExilesCyclingCardAndGrantsPlayPermission() {
        Card cyclingCard = new DrannithHealer();
        harness.setGraveyard(player1, List.of(cyclingCard));

        castRootingMoloch();
        harness.handleMultipleCardsChosen(player1, List.of(cyclingCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cyclingCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cyclingCard);
        assertThat(gd.exilePlayPermissions.get(cyclingCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("ETB does not target cycling cards in an opponent's graveyard")
    void etbDoesNotTargetOpponentGraveyard() {
        Card opponentCyclingCard = new DrannithHealer();
        harness.setGraveyard(player2, List.of(opponentCyclingCard));

        castRootingMoloch();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCyclingCard);
    }

    private void castRootingMoloch() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RootingMoloch()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
