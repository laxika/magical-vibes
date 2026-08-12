package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LivingArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and puts counters equal to the target creature's mana value")
    void sacrificesItselfAndUsesTargetManaValue() {
        Permanent armor = new Permanent(new LivingArmor());
        gd.playerBattlefields.get(player1.getId()).add(armor);
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Living Armor");
    }

    @Test
    @DisplayName("A zero-mana-value creature gets no counters")
    void zeroManaValueCreatureGetsNoCounters() {
        Permanent armor = new Permanent(new LivingArmor());
        gd.playerBattlefields.get(player1.getId()).add(armor);
        Permanent target = new Permanent(new Ornithopter());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Living Armor");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent armor = new Permanent(new LivingArmor());
        gd.playerBattlefields.get(player1.getId()).add(armor);
        Permanent target = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player2.getId()).add(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
