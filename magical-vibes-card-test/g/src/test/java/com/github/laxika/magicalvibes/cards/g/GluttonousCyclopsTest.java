package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


@CardUsed({GluttonousCyclops.class})
class GluttonousCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Gluttonous Cyclops")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent cyclops = addReadyCyclops();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cyclops.isMonstrous()).isTrue();
        assertThat(cyclops.getEffectivePower()).isEqualTo(8);
        assertThat(cyclops.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Monstrosity can be activated again but adds no further counters")
    void monstrosityCanBeActivatedAgainWithoutAddingCounters() {
        Permanent cyclops = addReadyCyclops();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cyclops.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Two pending monstrosity activations add counters only once")
    void pendingActivationsAddCountersOnlyOnce() {
        Permanent cyclops = addReadyCyclops();
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cyclops.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("A tapped summoning-sick Cyclops can activate monstrosity")
    void monstrosityDoesNotRequireTappingOrHaste() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new GluttonousCyclops());
        cyclops.setSummoningSick(true);
        cyclops.tap();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cyclops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cyclops.isMonstrous()).isTrue();
        assertThat(cyclops.isTapped()).isTrue();
    }
    private Permanent addReadyCyclops() {
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new GluttonousCyclops());
        cyclops.setSummoningSick(false);
        return cyclops;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
