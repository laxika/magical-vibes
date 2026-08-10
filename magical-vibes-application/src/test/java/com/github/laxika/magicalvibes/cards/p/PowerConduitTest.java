package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PowerConduitTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode removes a counter you control and puts a charge counter on target artifact")
    void artifactMode() {
        Permanent conduit = addConduit();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        conduit.setCounterCount(CounterType.CHARGE, 1);

        activate(0, artifact.getId());
        harness.passBothPriorities();

        assertThat(conduit.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature mode removes a counter you control and puts a +1/+1 counter on target creature")
    void creatureMode() {
        Permanent conduit = addConduit();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        conduit.setCounterCount(CounterType.CHARGE, 1);

        activate(1, creature.getId());
        harness.passBothPriorities();

        assertThat(conduit.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each mode rejects a target of the wrong type")
    void modesRequireTheirPrintedTargetType() {
        Permanent conduit = addConduit();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        conduit.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> activate(0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
        assertThat(conduit.isTapped()).isFalse();
        assertThat(conduit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without a counter on a permanent you control")
    void requiresControlledCounter() {
        addConduit();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    private Permanent addConduit() {
        return harness.addToBattlefieldAndReturn(player1, new PowerConduit());
    }

    private void activate(int abilityIndex, java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, abilityIndex, null, targetId);
    }
}
