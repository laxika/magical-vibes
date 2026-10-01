package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shinewend.class, Bitterblossom.class, IndomitableAncients.class})
class ShinewendTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter")
    void entersWithOnePlusOneCounter() {
        Permanent shinewend = harness.enterBattlefieldAndReturn(player1, new Shinewend());

        assertThat(shinewend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to destroy a target enchantment")
    void removesCounterAndDestroysTargetEnchantment() {
        Permanent shinewend = harness.enterBattlefieldAndReturn(player1, new Shinewend());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Bitterblossom());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, enchantment.getId());

        assertThat(shinewend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bitterblossom");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        Permanent shinewend = harness.enterBattlefieldAndReturn(player1, new Shinewend());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IndomitableAncients());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shinewend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Indomitable Ancients");
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent shinewend = harness.enterBattlefieldAndReturn(player1, new Shinewend());
        shinewend.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }
}
