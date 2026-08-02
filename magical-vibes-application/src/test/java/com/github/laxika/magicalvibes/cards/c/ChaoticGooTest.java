package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChaoticGooTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new ChaoticGoo()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent goo = goo();
        assertThat(goo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(goo.getEffectivePower()).isEqualTo(3);
        assertThat(goo.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep trigger prompts the controller and does nothing when declined")
    void decliningLeavesCountersUnchanged() {
        harness.addToBattlefield(player1, new ChaoticGoo());
        goo().setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(goo().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting flips a coin: a win adds a counter, a loss removes one")
    void acceptingFlipsCoin() {
        harness.addToBattlefield(player1, new ChaoticGoo());
        goo().setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(goo().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isIn(2, 4);
    }

    @Test
    @DisplayName("Losing the last counter leaves a 0/0 that dies to state-based actions")
    void losingLastCounterKillsIt() {
        harness.addToBattlefield(player1, new ChaoticGoo());
        goo().setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        boolean stillAlive = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Chaotic Goo"));
        if (stillAlive) {
            assertThat(goo().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        } else {
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Chaotic Goo"));
        }
    }

    private Permanent goo() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Chaotic Goo"))
                .findFirst().orElseThrow();
    }
}
