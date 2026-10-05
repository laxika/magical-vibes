package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerConduit.class, Ornithopter.class, FangrenHunter.class})
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FangrenHunter());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FangrenHunter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PowerConduit());
        conduit.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> activate(0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
        assertThatThrownBy(() -> activate(1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(conduit.isTapped()).isFalse();
        assertThat(conduit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter-removal cost can use any permanent you control")
    void removesCounterFromAnotherControlledPermanent() {
        Permanent conduit = addConduit();
        Permanent counterBearer = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        counterBearer.setCounterCount(CounterType.CHARGE, 1);

        activate(0, artifact.getId());
        harness.passBothPriorities();

        assertThat(conduit.isTapped()).isTrue();
        assertThat(counterBearer.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without a counter on a permanent you control")
    void requiresControlledCounter() {
        addConduit();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PowerConduit());

        assertThatThrownBy(() -> activate(0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("A permanent with different kinds of counters requires a counter choice")
    void controllerChoosesWhichKindOfCounterToRemove() {
        Permanent conduit = addConduit();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        conduit.setCounterCount(CounterType.CHARGE, 1);
        conduit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activate(0, artifact.getId());

        assertThat(conduit.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(conduit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.pendingInteractions).isNotEmpty();
    }

    @Test
    @DisplayName("A +1/+1 counter can pay for a charge counter on the same permanent")
    void convertsCounterOnTargetPermanent() {
        addConduit();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        activate(0, artifact.getId());

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters on opposing permanents cannot pay the activation cost")
    void cannotRemoveOpponentsCounter() {
        Permanent conduit = addConduit();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        artifact.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> activate(0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");

        assertThat(conduit.isTapped()).isFalse();
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power Conduit can target itself and must tap to pay its cost")
    void canTargetItselfButCannotActivateAgainWhileTapped() {
        Permanent conduit = addConduit();
        conduit.setCounterCount(CounterType.CHARGE, 2);

        activate(0, conduit.getId());
        harness.passBothPriorities();

        assertThat(conduit.isTapped()).isTrue();
        assertThat(conduit.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThatThrownBy(() -> activate(0, conduit.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(conduit.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
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
