package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonLeoProblemSolvers.class, Ornithopter.class, GrizzlyBears.class})
class DonLeoProblemSolversTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and returns an artifact and creature under their owners' control")
    void flickersTargetArtifactAndCreature() {
        harness.addToBattlefieldAndReturn(player1, new DonLeoProblemSolvers());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        UUID artifactId = artifact.getId();
        UUID creatureId = creature.getId();

        beginEndStepTrigger();

        PendingInteraction.PermanentChoice artifactChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(artifactChoice.validPermanentIds()).contains(artifactId);
        assertThat(artifactChoice.validPermanentIds()).doesNotContain(creatureId);
        harness.handlePermanentChosen(player1, artifactId);

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.validPermanentIds()).contains(creatureId);
        harness.handlePermanentChosen(player1, creatureId);

        harness.passBothPriorities();

        Permanent returnedArtifact = findPermanent(player1, "Ornithopter");
        Permanent returnedCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedArtifact.getId()).isNotEqualTo(artifactId);
        assertThat(returnedCreature.getId()).isNotEqualTo(creatureId);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Allows declining both optional targets and only offers legal permanents")
    void allowsDecliningTargets() {
        harness.addToBattlefieldAndReturn(player1, new DonLeoProblemSolvers());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginEndStepTrigger();

        PendingInteraction.PermanentChoice artifactChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(artifactChoice.validPermanentIds()).contains(artifact.getId());
        assertThat(artifactChoice.validPermanentIds()).doesNotContain(creature.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.validPermanentIds()).contains(creature.getId());
        assertThat(creatureChoice.validPermanentIds()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ornithopter").getId()).isEqualTo(artifact.getId());
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(creature.getId());
    }

    private void beginEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
