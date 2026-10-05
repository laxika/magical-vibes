package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoggSentry.class, GoblinRaider.class, HolyDay.class})
class MoggSentryTest extends BaseCardTest {

    private void opponentCastsSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GoblinRaider(), "{1}{R}");
    }

    private void opponentCastsInstantSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new HolyDay(), "{W}");
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

        opponentCastsInstantSpell();
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
    @DisplayName("Each opponent spell gives Mogg Sentry another +2/+2")
    void triggersAndStacksForEachOpponentSpell() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsSpell();
        resolveAllTriggers();
        opponentCastsSpell();
        harness.passBothPriorities(); // Resolve the second Mogg Sentry trigger

        assertThat(sentry().getPowerModifier()).isEqualTo(4);
        assertThat(sentry().getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when the controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new MoggSentry());

        harness.castFromHand(player1, new GoblinRaider(), "{1}{R}");

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Gets a separate +2/+2 boost for each opponent spell")
    void getsSeparateBoostForEachOpponentSpell() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsInstantSpell();
        opponentCastsInstantSpell();
        resolveAllTriggers();

        assertThat(sentry().getPowerModifier()).isEqualTo(4);
        assertThat(sentry().getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MoggSentry());

        opponentCastsSpell();
        harness.passBothPriorities(); // Resolve Mogg Sentry trigger

        assertThat(sentry().getPowerModifier()).isEqualTo(2);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent sentry = sentry();
        assertThat(sentry.getPowerModifier()).isEqualTo(0);
        assertThat(sentry.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent spells trigger during the controller's turn")
    void triggersDuringControllersTurn() {
        harness.addToBattlefield(player1, new MoggSentry());

        harness.castFromHand(player2, new HolyDay(), "{W}");
        harness.passBothPriorities();

        assertThat(sentry().getPowerModifier()).isEqualTo(2);
        assertThat(sentry().getToughnessModifier()).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Sentry boosts only itself when an opponent casts a spell")
    void eachSentryBoostsItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MoggSentry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MoggSentry());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new MoggSentry());

        opponentCastsInstantSpell();
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(2);
        assertThat(opposing.getPowerModifier()).isZero();
        assertThat(opposing.getToughnessModifier()).isZero();
    }

    private Permanent sentry() {
        return findPermanent(player1, "Mogg Sentry");
    }
}
