package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MoltenHydra.class)
class MoltenHydraTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability puts a +1/+1 counter on Molten Hydra")
    void firstAbilityAddsCounter() {
        Permanent hydra = addCreatureReady(player1, new MoltenHydra());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability can be activated while Molten Hydra is tapped")
    void firstAbilityDoesNotRequireUntappedHydra() {
        Permanent hydra = addCreatureReady(player1, new MoltenHydra());
        hydra.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hydra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability removes all counters and deals that much damage")
    void secondAbilityDealsDamageEqualToRemovedCounters() {
        Permanent hydra = addCreatureReady(player1, new MoltenHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(hydra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability deals no damage with no +1/+1 counters and preserves other counters")
    void secondAbilityWithNoCountersDealsNoDamage() {
        Permanent hydra = addCreatureReady(player1, new MoltenHydra());
        hydra.setCounterCount(CounterType.CHARGE, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(hydra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability can deal damage to a creature")
    void secondAbilityDealsDamageToCreature() {
        Permanent hydra = addCreatureReady(player1, new MoltenHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new MoltenHydra());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Molten Hydra");
        harness.assertInGraveyard(player2, "Molten Hydra");
    }

    @Test
    @DisplayName("The second ability cannot be activated while Molten Hydra is tapped")
    void secondAbilityRequiresUntappedHydra() {
        Permanent hydra = addCreatureReady(player1, new MoltenHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        hydra.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
