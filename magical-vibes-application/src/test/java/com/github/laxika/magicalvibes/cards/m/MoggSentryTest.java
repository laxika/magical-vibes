package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DestructiveFlow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoggSentry.class, MoggJailer.class, DestructiveFlow.class})
class MoggSentryTest extends BaseCardTest {

    private void opponentCastsSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MoggJailer(), "{1}{R}");
    }

    @Test
    @DisplayName("Triggers when an opponent casts a spell")
    void triggersWhenOpponentCastsSpell() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsSpell();

        assertThat(gd.stack)
                .filteredOn(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
    }

    @Test
    @DisplayName("Triggers when an opponent casts a noncreature spell")
    void triggersWhenOpponentCastsNoncreatureSpell() {
        harness.addToBattlefield(player1, new MoggSentry());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new DestructiveFlow(), "{B}{R}{G}");
        harness.passBothPriorities();

        assertThat(sentry().getPowerModifier()).isEqualTo(2);
        assertThat(sentry().getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving the trigger gives Mogg Sentry +2/+2 until end of turn")
    void resolvingTriggerBoostsSentry() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsSpell();
        harness.passBothPriorities(); // Resolve Mogg Sentry trigger

        Permanent sentry = sentry();
        assertThat(sentry.getPowerModifier()).isEqualTo(2);
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when the controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new MoggSentry());

        harness.castFromHand(player1, new MoggJailer(), "{1}{R}");

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsSpell();
        harness.passBothPriorities(); // Resolve Mogg Sentry trigger

        assertThat(sentry().getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent sentry = sentry();
        assertThat(sentry.getPowerModifier()).isEqualTo(0);
        assertThat(sentry.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent sentry() {
        return findPermanent(player1, "Mogg Sentry");
    }
}
