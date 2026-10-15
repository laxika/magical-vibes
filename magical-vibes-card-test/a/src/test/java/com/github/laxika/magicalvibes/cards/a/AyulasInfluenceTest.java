package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AyulasInfluence.class, SnowCoveredForest.class, AyulaQueenAmongBears.class})
class AyulasInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a land creates a 2/2 green Bear token")
    void discardingLandCreatesBearToken() {
        addInfluenceReady();
        harness.setHand(player1, List.of(new SnowCoveredForest()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        Permanent token = findPermanent(player1, "Bear");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getName()).isEqualTo("Bear");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Only land cards can pay the discard cost")
    void onlyLandCardsCanPayDiscardCost() {
        addInfluenceReady();
        harness.setHand(player1, List.of(new AyulaQueenAmongBears(), new SnowCoveredForest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without a land card in hand")
    void cannotActivateWithoutLandCard() {
        addInfluenceReady();
        harness.setHand(player1, List.of(new AyulaQueenAmongBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The land is discarded before the Bear is created on resolution")
    void discardIsPaidBeforeResolution() {
        addInfluenceReady();
        harness.setHand(player1, List.of(new SnowCoveredForest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.assertNotInHand(player1, "Snow-Covered Forest");
        assertThat(countPermanents(player1, "Bear")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
        assertThat(findPermanent(player1, "Bear").getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Each activation can discard another land without tapping the enchantment")
    void canActivateRepeatedly() {
        addInfluenceReady();
        harness.setHand(player1, List.of(new SnowCoveredForest(), new SnowCoveredForest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bear")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Ayula's Influence").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated during the opponent's upkeep")
    void canActivateDuringOpponentsTurn() {
        addInfluenceReady();
        harness.setHand(player1, List.of(new SnowCoveredForest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bear")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bear")).isZero();
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
    }

    private void addInfluenceReady() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new AyulasInfluence());
    }
}
