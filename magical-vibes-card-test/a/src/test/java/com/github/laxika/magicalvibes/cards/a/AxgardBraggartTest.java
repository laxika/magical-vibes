package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AxgardBraggart.class})
class AxgardBraggartTest extends BaseCardTest {

    @Test
    @DisplayName("Boast untaps Axgard Braggart and puts a +1/+1 counter on it")
    void boastUntapsAndAddsCounter() {
        Permanent braggart = addCreatureReady(player1, new AxgardBraggart());
        braggart.tap();
        braggart.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(braggart.isTapped()).isFalse();
        assertThat(braggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boast cannot be activated if Axgard Braggart did not attack this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        addCreatureReady(player1, new AxgardBraggart());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent braggart = addCreatureReady(player1, new AxgardBraggart());
        braggart.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast adds a counter even if the creature is already untapped")
    void boastWorksWhileUntapped() {
        Permanent braggart = addCreatureReady(player1, new AxgardBraggart());
        braggart.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(braggart.isTapped()).isFalse();
        assertThat(braggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boast's activation limit applies before the first activation resolves")
    void boastCannotBeActivatedAgainInResponse() {
        Permanent braggart = addCreatureReady(player1, new AxgardBraggart());
        braggart.tap();
        braggart.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(braggart.isTapped()).isTrue();
        assertThat(braggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();

        assertThat(braggart.isTapped()).isFalse();
        assertThat(braggart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Axgard Braggart has its own boast activation limit")
    void boastLimitIsPerPermanent() {
        Permanent first = addCreatureReady(player1, new AxgardBraggart());
        Permanent second = addCreatureReady(player1, new AxgardBraggart());
        first.setAttackedThisTurn(true);
        second.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
