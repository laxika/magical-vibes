package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnarlidPack.class})
class GnarlidPackTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without +1/+1 counters when not multikicked")
    void entersWithoutCountersWhenNotMultikicked() {
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gnarlidPack = findGnarlidPack();
        assertThat(gnarlidPack).isNotNull();
        assertThat(gnarlidPack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with one +1/+1 counter per multikicker payment")
    void entersWithCountersForEachMultikickerPayment() {
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{G}", "{1}{G}"));
        harness.passBothPriorities();

        Permanent gnarlidPack = findGnarlidPack();
        assertThat(gnarlidPack).isNotNull();
        assertThat(gnarlidPack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A single multikicker payment adds one counter without a triggered ability")
    void entersWithOneCounterWhenKickedOnce() {
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{G}"));
        harness.passBothPriorities();

        Permanent gnarlidPack = findGnarlidPack();
        assertThat(gnarlidPack).isNotNull();
        assertThat(gnarlidPack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multikicker cannot be paid with only enough mana for the base spell")
    void rejectsUnaffordableMultikickerPayment() {
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of("{1}{G}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Gnarlid Pack");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each multikicker payment requires another green mana")
    void rejectsMultikickerWithoutAdditionalGreenMana() {
        harness.setHand(player1, List.of(new GnarlidPack()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of("{1}{G}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Gnarlid Pack");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent findGnarlidPack() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gnarlid Pack"))
                .findFirst()
                .orElse(null);
    }
}
