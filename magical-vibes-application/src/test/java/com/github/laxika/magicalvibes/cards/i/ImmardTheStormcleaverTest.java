package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImmardTheStormcleaver.class})
class ImmardTheStormcleaverTest extends BaseCardTest {

    private static final String PUT_CHARGE_COUNTER = "Put a charge counter on Immard";
    private static final String REMOVE_CHARGE_COUNTER = "Remove a charge counter from Immard";
    private static final String DEAL_DAMAGE = "Immard deals 4 damage to any target";
    private static final String GRANT_KEYWORDS =
            "Immard gains lifelink and indestructible until end of turn";

    @Test
    @DisplayName("Entering the battlefield can put a charge counter on Immard")
    void enteringCanPutChargeCounter() {
        harness.castFromHand(player1, new ImmardTheStormcleaver(), "{1}{U}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, PUT_CHARGE_COUNTER);

        Permanent immard = findPermanent(player1, "Immard, the Stormcleaver");
        assertThat(immard.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking can remove a charge counter and deal 4 damage to any target")
    void attackingRemovesCounterAndDealsDamage() {
        Permanent immard = addCreatureReady(player1, new ImmardTheStormcleaver());
        immard.setCounterCount(CounterType.CHARGE, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleListChoice(player1, REMOVE_CHARGE_COUNTER);
            harness.handleListChoice(player1, DEAL_DAMAGE);
            harness.handlePermanentChosen(player1, player2.getId());
            assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
            harness.passBothPriorities();
        });

        assertThat(immard.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Removing a counter can grant lifelink and indestructible until end of turn")
    void attackingCanGrantKeywordsUntilEndOfTurn() {
        Permanent immard = addCreatureReady(player1, new ImmardTheStormcleaver());
        immard.setCounterCount(CounterType.CHARGE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleListChoice(player1, REMOVE_CHARGE_COUNTER);
            harness.handleListChoice(player1, GRANT_KEYWORDS);
            assertThat(gqs.hasKeyword(gd, immard, Keyword.LIFELINK)).isFalse();
            assertThat(gqs.hasKeyword(gd, immard, Keyword.INDESTRUCTIBLE)).isFalse();
            harness.passBothPriorities();
        });

        assertThat(gqs.hasKeyword(gd, immard, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, immard, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, immard, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, immard, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The counter choice is made only when the original trigger resolves")
    void counterChoiceWaitsForResolution() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castFromHand(player1, new ImmardTheStormcleaver(), "{1}{U}{R}{W}");
            harness.passBothPriorities();

            Permanent immard = findPermanent(player1, "Immard, the Stormcleaver");
            assertThat(immard.getCounterCount(CounterType.CHARGE)).isZero();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();
            harness.handleListChoice(player1, PUT_CHARGE_COUNTER);
            assertThat(immard.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Removing a nonexistent charge counter is not a legal choice")
    void cannotChooseRemovalWithoutChargeCounter() {
        harness.castFromHand(player1, new ImmardTheStormcleaver(), "{1}{U}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice choice) {
            assertThat(choice.options()).doesNotContain(REMOVE_CHARGE_COUNTER);
            harness.handleListChoice(player1, PUT_CHARGE_COUNTER);
        }
        assertThat(findPermanent(player1, "Immard, the Stormcleaver")
                .getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking can add a charge counter even when one is already present")
    void attackingCanAddAnotherChargeCounter() {
        Permanent immard = addCreatureReady(player1, new ImmardTheStormcleaver());
        immard.setCounterCount(CounterType.CHARGE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleListChoice(player1, PUT_CHARGE_COUNTER);
            assertThat(immard.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("The enter trigger can remove one charge counter without removing other counters")
    void enteringCanRemoveOneChargeCounterAndGrantKeywords() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castFromHand(player1, new ImmardTheStormcleaver(), "{1}{U}{R}{W}");
            harness.passBothPriorities();
            Permanent immard = findPermanent(player1, "Immard, the Stormcleaver");
            immard.setCounterCount(CounterType.CHARGE, 2);
            immard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

            harness.passBothPriorities();
            harness.handleListChoice(player1, REMOVE_CHARGE_COUNTER);
            harness.handleListChoice(player1, GRANT_KEYWORDS);
            harness.passBothPriorities();

            assertThat(immard.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
            assertThat(immard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, immard, Keyword.LIFELINK)).isTrue();
            assertThat(gqs.hasKeyword(gd, immard, Keyword.INDESTRUCTIBLE)).isTrue();
        });
    }
}
