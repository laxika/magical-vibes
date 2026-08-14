package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnergyChamberTest extends BaseCardTest {

    private static final String PLUS_ONE_MODE = "Put a +1/+1 counter on target artifact creature.";
    private static final String CHARGE_MODE = "Put a charge counter on target noncreature artifact.";

    @Test
    @DisplayName("The +1/+1 mode targets an artifact creature")
    void putsPlusOneCounterOnArtifactCreature() {
        harness.addToBattlefield(player1, new EnergyChamber());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, PLUS_ONE_MODE);
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The charge mode targets a noncreature artifact")
    void putsChargeCounterOnNoncreatureArtifact() {
        harness.addToBattlefield(player1, new EnergyChamber());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, CHARGE_MODE);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(artifactCreature.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Each mode rejects the wrong artifact type")
    void modesRejectWrongArtifactType() {
        harness.addToBattlefield(player1, new EnergyChamber());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, PLUS_ONE_MODE);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, CHARGE_MODE);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The same mode can be chosen on later upkeeps")
    void modesAreNotConsumed() {
        harness.addToBattlefield(player1, new EnergyChamber());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, CHARGE_MODE);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, CHARGE_MODE);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }
}
