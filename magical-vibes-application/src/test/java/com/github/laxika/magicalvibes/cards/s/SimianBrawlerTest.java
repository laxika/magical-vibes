package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({SimianBrawler.class, SnowCoveredForest.class, BorealDruid.class})
class SimianBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a land card gives Simian Brawler +1/+1 until end of turn")
    void discardingLandBoostsSimianBrawler() {
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        int basePower = gqs.getEffectivePower(gd, brawler);
        int baseToughness = gqs.getEffectiveToughness(gd, brawler);
        harness.setHand(player1, List.of(new SnowCoveredForest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The +1/+1 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        int basePower = gqs.getEffectivePower(gd, brawler);
        int baseToughness = gqs.getEffectiveToughness(gd, brawler);
        harness.setHand(player1, List.of(new SnowCoveredForest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(baseToughness + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot activate without a land card to discard")
    void cannotActivateWithoutLandCard() {
        harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        Card nonland = new BorealDruid();
        harness.setHand(player1, List.of(nonland));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only land cards are offered for the discard cost")
    void onlyLandCardsCanBeDiscarded() {
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        Card nonland = new BorealDruid();
        Card land = new SnowCoveredForest();
        harness.setHand(player1, List.of(nonland, land));

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.DiscardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardCostChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
    }

    @Test
    @DisplayName("The land is discarded as a cost before the boost resolves")
    void discardIsPaidBeforeResolution() {
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        int basePower = gqs.getEffectivePower(gd, brawler);
        int baseToughness = gqs.getEffectiveToughness(gd, brawler);
        harness.setHand(player1, List.of(new SnowCoveredForest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.assertNotInHand(player1, "Snow-Covered Forest");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(baseToughness);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("A tapped Simian Brawler can activate repeatedly and the boosts accumulate")
    void tappedBrawlerCanActivateRepeatedly() {
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        brawler.setTapped(true);
        int basePower = gqs.getEffectivePower(gd, brawler);
        int baseToughness = gqs.getEffectiveToughness(gd, brawler);
        harness.setHand(player1, List.of(new SnowCoveredForest(), new SnowCoveredForest()));

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(baseToughness + 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(baseToughness);
    }
}
