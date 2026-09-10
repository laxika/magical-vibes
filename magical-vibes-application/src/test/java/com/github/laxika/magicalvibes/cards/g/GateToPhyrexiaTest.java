package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GateToPhyrexia.class, FountainOfYouth.class, GrizzlyBears.class})
class GateToPhyrexiaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature to destroy a target artifact")
    void sacrificesCreatureToDestroyArtifact() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new GateToPhyrexia());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareControllerUpkeep();

        harness.activateAbility(player1, battlefieldIndex(gate), null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gate).doesNotContain(fodder);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Can be activated only once during its controller's upkeep")
    void limitedToOnceDuringControllerUpkeep() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new GateToPhyrexia());
        Permanent firstFodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondFodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareControllerUpkeep();

        harness.activateAbility(player1, battlefieldIndex(gate), null, firstArtifact.getId());
        harness.handlePermanentChosen(player1, firstFodder.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gate), null, secondArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondFodder);
    }

    @Test
    @DisplayName("Cannot activate outside its controller's upkeep or target a non-artifact")
    void requiresControllerUpkeepAndArtifactTarget() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new GateToPhyrexia());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gate), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        prepareControllerUpkeep();
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gate), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    private void prepareControllerUpkeep() {
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
