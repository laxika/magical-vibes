package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonLeoProblemSolvers.class, Ornithopter.class, GrizzlyBears.class, SoulWarden.class})
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

    @Test
    @DisplayName("An artifact creature can be chosen for both targets and returns once")
    void canChooseSameArtifactCreatureForBothTargets() {
        harness.addToBattlefield(player1, new DonLeoProblemSolvers());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        beginEndStepTrigger();
        harness.handlePermanentChosen(player1, artifactCreature.getId());

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.validPermanentIds()).contains(artifactCreature.getId());
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ornithopter").getId()).isNotEqualTo(artifactCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Ornithopter"))
                .hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both targets return together under their owners' control")
    void stolenWardenSeesArtifactReturnUnderOwnersControl() {
        harness.addToBattlefield(player1, new DonLeoProblemSolvers());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        SoulWarden wardenCard = new SoulWarden();
        wardenCard.setOwnerId(player2.getId());
        Permanent warden = harness.addToBattlefieldAndReturn(player1, wardenCard);
        gd.stolenCreatures.put(warden.getId(), player2.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Control effect", null,
                player1.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT), warden.getId(),
                null, null, EffectDuration.PERMANENT, 0));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginEndStepTrigger();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, warden.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Soul Warden");
        harness.assertOnBattlefield(player2, "Soul Warden");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Can choose only the creature, including Don and Leo itself")
    void canDeclineArtifactAndFlickerItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DonLeoProblemSolvers());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        beginEndStepTrigger();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Don & Leo, Problem Solvers").getId()).isNotEqualTo(source.getId());
        assertThat(findPermanent(player1, "Ornithopter").getId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Can choose only the artifact")
    void canDeclineCreatureAndFlickerArtifact() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DonLeoProblemSolvers());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        beginEndStepTrigger();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Don & Leo, Problem Solvers").getId()).isEqualTo(source.getId());
        assertThat(findPermanent(player1, "Ornithopter").getId()).isNotEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Does not trigger on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DonLeoProblemSolvers());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Don & Leo, Problem Solvers").getId()).isEqualTo(source.getId());
    }

    private void beginEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
