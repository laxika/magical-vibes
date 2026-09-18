package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.t.TangleAsp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnergyChamber.class, Arachnoid.class, ConjurersBauble.class, TangleAsp.class})
class EnergyChamberTest extends BaseCardTest {

    private static final String PLUS_ONE_MODE = "Put a +1/+1 counter on target artifact creature.";
    private static final String CHARGE_MODE = "Put a charge counter on target noncreature artifact.";

    @Test
    @DisplayName("The +1/+1 mode targets an artifact creature")
    void putsPlusOneCounterOnArtifactCreature() {
        harness.addToBattlefield(player1, new EnergyChamber());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());

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
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());

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
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());

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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());

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

    @Test
    @DisplayName("Each mode can target an artifact controlled by an opponent")
    void modesCanTargetOpposingArtifacts() {
        harness.addToBattlefield(player1, new EnergyChamber());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, PLUS_ONE_MODE);
        harness.handlePermanentChosen(player1, opponentArtifactCreature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, CHARGE_MODE);
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(opponentArtifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentArtifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Neither mode can target a non-artifact permanent")
    void modesRejectNonArtifactPermanents() {
        harness.addToBattlefield(player1, new EnergyChamber());
        Permanent nonArtifactCreature = harness.addToBattlefieldAndReturn(player1, new TangleAsp());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, PLUS_ONE_MODE);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonArtifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, CHARGE_MODE);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonArtifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The trigger fires only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new EnergyChamber());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());

        advanceToUpkeep(player2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, PLUS_ONE_MODE);
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
