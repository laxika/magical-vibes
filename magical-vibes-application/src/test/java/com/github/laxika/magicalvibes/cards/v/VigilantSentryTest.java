package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuntailHawk.class, VigilantSentry.class})
class VigilantSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when its controller has threshold")
    void getsThresholdBonus() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent sentry = addReadySentry();

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    @DisplayName("Has no threshold bonus below seven cards")
    void hasNoThresholdBonusBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        Permanent sentry = addReadySentry();

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threshold uses only the creature's controller's graveyard")
    void thresholdUsesOnlyControllerGraveyard() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent sentry = addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        target.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Threshold effects turn off when the graveyard drops below seven cards")
    void thresholdEffectsTurnOffBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent sentry = addReadySentry();

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps to give an attacking creature +3/+3 at threshold")
    void boostsAttackingCreatureAtThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent sentry = addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        target.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(sentry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps to give a blocking creature +3/+3 at threshold")
    void boostsBlockingCreatureAtThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        target.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The granted boost ends at the end of the turn")
    void grantedBoostEndsAtEndOfTurn() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent sentry = addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        target.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost does not resolve if its target stops attacking")
    void boostFizzlesIfTargetStopsAttackingBeforeResolution() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        target.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    private Permanent addReadySentry() {
        return addCreatureReady(player1, new VigilantSentry());
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new SuntailHawk(), new SuntailHawk(), new SuntailHawk(), new SuntailHawk(),
                new SuntailHawk(), new SuntailHawk(), new SuntailHawk());
    }

    @Test
    @DisplayName("Threshold counts cards in the source controller's graveyard")
    void thresholdCountsSourceControllersGraveyard() {
        harness.setGraveyard(player2, graveyardWithCardsForJudReview(7));
        Permanent sentry = addReadySentry();

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not grant the tap ability below threshold")
    void doesNotGrantTapAbilityBelowThreshold() {
        harness.setGraveyard(player1, graveyardWithCardsForJudReview(6));
        addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        target.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted boost wears off at end of turn")
    void grantedBoostWearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, graveyardWithCardsForJudReview(7));
        addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        target.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the granted ability while Vigilant Sentry is tapped")
    void cannotActivateWhileTapped() {
        harness.setGraveyard(player1, graveyardWithCardsForJudReview(7));
        Permanent sentry = addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        target.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(sentry.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> graveyardWithCardsForJudReview(int count) {
        List<Card> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cards.add(new SuntailHawk());
        }
        return cards;
    }
}
