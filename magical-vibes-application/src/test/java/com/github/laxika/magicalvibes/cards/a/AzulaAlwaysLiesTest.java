package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzulaAlwaysLies.class, GrizzlyBears.class})
class AzulaAlwaysLiesTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode gives a creature -1/-1 until end of turn")
    void weakensTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("The second mode puts a +1/+1 counter on a creature")
    void putsCounterOnTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{1}, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both modes can target the same creature")
    void bothModesResolveOnSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{0, 1}, List.of(creature.getId(), creature.getId()));

        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter mode rejects a player target")
    void counterModeRequiresCreatureTarget() {
        harness.setHand(player1, List.of(new AzulaAlwaysLies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new AzulaAlwaysLies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
