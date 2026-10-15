package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GriffinSentinel;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfTheSun.class, GriffinSentinel.class, GreenwoodSentinel.class})
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
        Permanent target = addCreatureReady(player2, new GreenwoodSentinel());
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

    @Test
    @DisplayName("Can target another Herald of the Sun controlled by its controller")
    void canTargetAnotherHerald() {
        addCreatureReady(player1, new HeraldOfTheSun());
        Permanent target = addCreatureReady(player1, new HeraldOfTheSun());
        addMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Herald can activate repeatedly by paying each time")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfTheSun());
        herald.setSummoningSick(true);
        herald.tap();
        Permanent target = addCreatureReady(player1, new GriffinSentinel());
        addMana();
        addMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate with only three mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new HeraldOfTheSun());
        Permanent target = addCreatureReady(player2, new GriffinSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the white mana requirement with colorless mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new HeraldOfTheSun());
        Permanent target = addCreatureReady(player2, new GriffinSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
