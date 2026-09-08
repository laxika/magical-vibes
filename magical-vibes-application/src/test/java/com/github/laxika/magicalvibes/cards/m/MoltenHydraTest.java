package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoltenHydraTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability puts a +1/+1 counter on Molten Hydra")
    void firstAbilityAddsCounter() {
        Permanent hydra = addHydra(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability removes all counters and deals that much damage")
    void secondAbilityDealsDamageEqualToRemovedCounters() {
        Permanent hydra = addHydra(player1);
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(hydra.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability cannot be activated while Molten Hydra is tapped")
    void secondAbilityRequiresUntappedHydra() {
        Permanent hydra = addHydra(player1);
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        hydra.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHydra(Player player) {
        Permanent hydra = harness.addToBattlefieldAndReturn(player, new MoltenHydra());
        hydra.setSummoningSick(false);
        return hydra;
    }
}
