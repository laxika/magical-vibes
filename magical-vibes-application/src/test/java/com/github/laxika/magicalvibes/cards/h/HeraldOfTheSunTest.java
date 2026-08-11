package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GriffinSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeraldOfTheSunTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on another creature with flying")
    void putsCounterOnFlyingCreature() {
        addCreatureReady(player1, new HeraldOfTheSun());
        Permanent target = addCreatureReady(player2, new GriffinSentinel());
        addMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        addCreatureReady(player1, new HeraldOfTheSun());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target Herald of the Sun itself")
    void cannotTargetItself() {
        Permanent herald = addCreatureReady(player1, new HeraldOfTheSun());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, herald.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
