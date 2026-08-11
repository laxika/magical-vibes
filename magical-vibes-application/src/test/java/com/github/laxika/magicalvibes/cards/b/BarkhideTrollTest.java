package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarkhideTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Barkhide Troll enters with a +1/+1 counter")
    void entersWithPlusOneCounter() {
        harness.setHand(player1, List.of(new BarkhideTroll()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Barkhide Troll");
        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter grants hexproof until end of turn")
    void removesCounterAndGrantsHexproof() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Granted hexproof wears off at end of turn")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent troll = addTrollReady(player1);
        troll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, troll, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without a +1/+1 counter")
    void cannotActivateWithoutPlusOneCounter() {
        addTrollReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTrollReady(Player player) {
        return addCreatureReady(player, new BarkhideTroll());
    }
}
