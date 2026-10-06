package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SkitterOfLizards.class})
class SkitterOfLizardsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without +1/+1 counters when not multikicked")
    void entersWithoutCountersWhenNotMultikicked() {
        harness.setHand(player1, List.of(new SkitterOfLizards()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent skitter = findSkitter();
        assertThat(skitter).isNotNull();
        assertThat(skitter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with one +1/+1 counter per multikicker payment")
    void entersWithCountersForEachMultikickerPayment() {
        harness.setHand(player1, List.of(new SkitterOfLizards()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{R}", "{1}{R}"));
        harness.passBothPriorities();

        Permanent skitter = findSkitter();
        assertThat(skitter).isNotNull();
        assertThat(skitter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A single multikicker payment adds one counter before priority resumes")
    void entersWithOneCounterWhenKickedOnce() {
        harness.setHand(player1, List.of(new SkitterOfLizards()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{R}"));
        harness.passBothPriorities();

        assertThat(findSkitter().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multikicker requires its red mana in addition to the base red cost")
    void cannotPayMultikickerWithOnlyGenericMana() {
        harness.setHand(player1, List.of(new SkitterOfLizards()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of("{1}{R}")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Skitter of Lizards");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Haste allows attacking on the turn it was cast without multikicker")
    void canAttackImmediatelyWithoutMultikicker() {
        harness.setHand(player1, List.of(new SkitterOfLizards()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(findSkitter().isAttacking()).isTrue();
    }

    private Permanent findSkitter() {
        return gqs.findPermanentById(gd, harness.getPermanentId(player1, "Skitter of Lizards"));
    }
}
