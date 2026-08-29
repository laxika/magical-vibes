package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleetfeatherCockatriceTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Fleetfeather Cockatrice")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent cockatrice = addCreatureReady(player1, new FleetfeatherCockatrice());
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cockatrice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(cockatrice.isMonstrous()).isTrue();
        assertThat(cockatrice.getEffectivePower()).isEqualTo(6);
        assertThat(cockatrice.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Monstrosity cannot be activated again after it resolves")
    void monstrosityOnlyResolvesOnce() {
        Permanent cockatrice = addCreatureReady(player1, new FleetfeatherCockatrice());
        addMonstrosityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private void addMonstrosityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
