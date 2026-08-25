package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VigilantSentry.class, GrizzlyBears.class})
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
        Permanent sentry = addReadySentry();

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps to give an attacking creature +3/+3 at threshold")
    void boostsAttackingCreatureAtThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent sentry = addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(sentry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps to give a blocking creature +3/+3 at threshold")
    void boostsBlockingCreatureAtThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        addReadySentry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    private Permanent addReadySentry() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new VigilantSentry());
        sentry.setSummoningSick(false);
        return sentry;
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
