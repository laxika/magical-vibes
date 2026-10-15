package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaoticGoo.class})
class ChaoticGooTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new ChaoticGoo()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent goo = findPermanent(player1, "Chaotic Goo");
        assertThat(goo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(goo.getEffectivePower()).isEqualTo(3);
        assertThat(goo.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep trigger prompts the controller and does nothing when declined")
    void decliningLeavesCountersUnchanged() {
        harness.addToBattlefieldAndReturn(player1, new ChaoticGoo())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Chaotic Goo")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting flips a coin: a win adds a counter, a loss removes one")
    void acceptingFlipsCoin() {
        harness.addToBattlefieldAndReturn(player1, new ChaoticGoo())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Chaotic Goo")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isIn(2, 4);
    }

    @Test
    @DisplayName("Losing the last counter leaves a 0/0 that dies to state-based actions")
    void losingLastCounterKillsIt() {
        harness.addToBattlefieldAndReturn(player1, new ChaoticGoo())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        boolean stillAlive = !findPermanents(player1, "Chaotic Goo").isEmpty();
        if (stillAlive) {
            assertThat(findPermanent(player1, "Chaotic Goo")
                    .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        } else {
            harness.assertInGraveyard(player1, "Chaotic Goo");
        }
    }

    @Test
    @DisplayName("Upkeep ability triggers only during its controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new ChaoticGoo())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Chaotic Goo")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two Goos have independent upkeep choices and only the accepted source changes")
    void multipleGoosResolveIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ChaoticGoo());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ChaoticGoo());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(List.of(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE),
                second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)))
                .contains(3)
                .satisfies(counts -> assertThat(counts.stream().mapToInt(Integer::intValue).sum()).isIn(5, 7));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
