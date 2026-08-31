package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FellwarStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.ScarwoodGoblins;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingArmor.class, ScarwoodGoblins.class, Ornithopter.class, FellwarStone.class})
class LivingArmorTest extends BaseCardTest {
    @Test
    void cannotActivateWhileTapped() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new LivingArmor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScarwoodGoblins());
        armor.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysCostsBeforeResolution() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new LivingArmor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScarwoodGoblins());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, armor.getCard().getName());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(armor);
        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifices itself and puts counters equal to the target creature's mana value")
    void sacrificesItselfAndUsesTargetManaValue() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new LivingArmor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScarwoodGoblins());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Living Armor");
    }

    @Test
    @DisplayName("A zero-mana-value creature gets no counters")
    void zeroManaValueCreatureGetsNoCounters() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new LivingArmor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Living Armor");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new LivingArmor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FellwarStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
