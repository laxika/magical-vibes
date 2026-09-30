package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SimicRagworm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExperimentKraj.class, SimicRagworm.class})
class ExperimentKrajTest extends BaseCardTest {

    @Test
    @DisplayName("Gains and uses an activated ability from each other creature with a +1/+1 counter")
    void gainsActivatedAbilityFromCounteredCreature() {
        Permanent kraj = addCreatureReady(player1, new ExperimentKraj());
        Permanent ragworm = addCreatureReady(player2, new SimicRagworm());
        ragworm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        kraj.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kraj.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Stops gaining an activated ability when the creature loses its +1/+1 counter")
    void stopsGainingAbilityWhenCounterIsRemoved() {
        addCreatureReady(player1, new ExperimentKraj());
        Permanent ragworm = addCreatureReady(player2, new SimicRagworm());
        ragworm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        ragworm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its tap ability puts a +1/+1 counter on a target creature")
    void putsCounterOnTargetCreature() {
        addCreatureReady(player1, new ExperimentKraj());
        Permanent ragworm = addCreatureReady(player2, new SimicRagworm());

        harness.activateAbility(player1, 0, 0, null, ragworm.getId());
        harness.passBothPriorities();

        assertThat(ragworm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays the copied activated ability with its required mana color")
    void copiedAbilityKeepsItsManaColor() {
        addCreatureReady(player1, new ExperimentKraj());
        Permanent ragworm = addCreatureReady(player2, new SimicRagworm());
        ragworm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not copy its own activated ability when it has a +1/+1 counter")
    void doesNotCopyItsOwnActivatedAbility() {
        addCreatureReady(player1, new ExperimentKraj())
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its tap ability cannot target a player")
    void tapAbilityRequiresCreatureTarget() {
        addCreatureReady(player1, new ExperimentKraj());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
