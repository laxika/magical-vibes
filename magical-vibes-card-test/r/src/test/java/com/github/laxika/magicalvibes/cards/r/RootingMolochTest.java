package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrannithHealer;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootingMoloch.class, DrannithHealer.class, AlmightyBrushwagg.class})
class RootingMolochTest extends BaseCardTest {

    @Test
    @DisplayName("ETB only targets a cycling card from your graveyard")
    void etbOnlyTargetsOwnCyclingCard() {
        Card cyclingCard = new DrannithHealer();
        Card nonCyclingCard = new AlmightyBrushwagg();
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

    @Test
    void cyclingDiscardsMolochAndDrawsWithoutTriggeringItsEtb() {
        Card moloch = new RootingMoloch();
        Card drawn = new DrannithHealer();
        Card graveyardCard = new DrannithHealer();
        harness.setHand(player1, List.of(moloch));
        harness.setLibrary(player1, List.of(drawn));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(moloch, graveyardCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledCreatureRequiresNormalManaAndCanBeCast() {
        Card cyclingCard = new DrannithHealer();
        harness.setGraveyard(player1, List.of(cyclingCard));
        castRootingMoloch();
        harness.handleMultipleCardsChosen(player1, List.of(cyclingCard.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, cyclingCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cyclingCard);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, cyclingCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drannith Healer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(cyclingCard);
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionIsNotExiled() {
        Card cyclingCard = new DrannithHealer();
        castWithGraveyardTarget(cyclingCard);
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(cyclingCard));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cyclingCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(cyclingCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(cyclingCard.getId());
    }

    @Test
    void playPermissionExpiresAfterControllersNextTurn() {
        Card cyclingCard = new DrannithHealer();
        harness.setLibrary(player1, List.of(new DrannithHealer(), new DrannithHealer()));
        harness.setLibrary(player2, List.of(new DrannithHealer(), new DrannithHealer()));
        castWithGraveyardTarget(cyclingCard);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(cyclingCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(cyclingCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(cyclingCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cyclingCard);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, cyclingCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithGraveyardTarget(Card target) {
        harness.setGraveyard(player1, List.of(target));
        castRootingMoloch();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
    }
    private void castRootingMoloch() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new RootingMoloch(), "{4}{R}");
        harness.passBothPriorities();
    }
}
