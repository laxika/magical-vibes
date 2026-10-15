package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DocOckEvilInventor.class, Millstone.class, Ornithopter.class, GrizzlyBears.class})
class DocOckEvilInventorTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, a noncreature artifact you control becomes an 8/8 Robot Villain")
    void animatesTargetArtifactPermanently() {
        addCreatureReady(player1, new DocOckEvilInventor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        advanceToCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId())
                .doesNotContain(player2.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.ROBOT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.VILLAIN)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.ROBOT)).isTrue();
    }

    @Test
    @DisplayName("The beginning-of-combat target must be a noncreature artifact you control")
    void onlyTargetsControlledNoncreatureArtifacts() {
        addCreatureReady(player1, new DocOckEvilInventor());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent ownCreatureArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        advanceToCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownArtifact.getId())
                .doesNotContain(ownCreatureArtifact.getId(), ownCreature.getId(), opponentArtifact.getId());
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        addCreatureReady(player1, new DocOckEvilInventor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    void doesNotQueueAbilityWithoutLegalTargets() {
        addCreatureReady(player1, new DocOckEvilInventor());
        Permanent creatureArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        advanceToCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creatureArtifact)).isZero();
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();
    }

    @Test
    void laterCombatAnimatesAnotherArtifactAndExcludesAlreadyAnimatedArtifact() {
        addCreatureReady(player1, new DocOckEvilInventor());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        advanceToCombat();
        harness.handlePermanentChosen(player1, firstArtifact.getId());
        harness.passBothPriorities();
        declareAttackers(player1, List.of());

        advanceToCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(secondArtifact.getId())
                .doesNotContain(firstArtifact.getId());
        harness.handlePermanentChosen(player1, secondArtifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstArtifact)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, firstArtifact)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, secondArtifact)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, secondArtifact)).isEqualTo(8);
        assertThat(gqs.hasEffectiveSubtype(gd, secondArtifact, CardSubtype.ROBOT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, secondArtifact, CardSubtype.VILLAIN)).isTrue();
    }

    private void advanceToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> harness.passBothPriorities());
    }
}
