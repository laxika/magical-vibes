package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
