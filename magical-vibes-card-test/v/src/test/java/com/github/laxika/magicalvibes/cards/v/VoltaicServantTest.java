package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoltaicServant.class, IcyManipulator.class, BalothGorger.class})
class VoltaicServantTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped artifact at controller's end step")
    void untapsTappedArtifactAtEndStep() {
        harness.addToBattlefield(player1, new VoltaicServant());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        UUID artifactId = artifact.getId();
        artifact.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step → triggers end step ability
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // Should be awaiting target selection for the artifact
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the tapped Icy Manipulator
        harness.handlePermanentChosen(player1, artifactId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Artifact should be untapped
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap an opponent's artifact")
    void canUntapOpponentArtifact() {
        harness.addToBattlefield(player1, new VoltaicServant());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        UUID artifactId = artifact.getId();
        artifact.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target itself (Voltaic Servant is an artifact creature)")
    void canTargetItself() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new VoltaicServant());
        UUID servantId = servant.getId();
        servant.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, servantId);
        harness.passBothPriorities();

        assertThat(servant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new VoltaicServant());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        artifact.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        // Advance to end step on opponent's turn
        gs.advanceStep(gd);

        // No trigger should fire for player1's Voltaic Servant
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        // Artifact should remain tapped
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-artifact creatures are not valid targets")
    void nonArtifactCreatureNotValidTarget() {
        harness.addToBattlefield(player1, new VoltaicServant());
        // Only non-artifact creature on the battlefield besides Voltaic Servant
        harness.addToBattlefield(player2, new BalothGorger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // Should be awaiting target selection — only Voltaic Servant itself should be valid
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Baloth Gorger (non-artifact) should NOT be in valid choices
        UUID gorgerId = harness.getPermanentId(player2, "Baloth Gorger");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(gorgerId);

        // Voltaic Servant (artifact creature) should be a valid target
        UUID servantId = harness.getPermanentId(player1, "Voltaic Servant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(servantId);
    }

    @Test
    @DisplayName("Can target an already untapped artifact (no-op untap)")
    void canTargetAlreadyUntappedArtifact() {
        harness.addToBattlefield(player1, new VoltaicServant());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        UUID artifactId = artifact.getId();

        // Artifact is already untapped
        assertThat(artifact.isTapped()).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        // Artifact remains untapped
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Triggered untap resolves after Voltaic Servant leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new VoltaicServant());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        artifact.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        gd.playerBattlefields.get(player1.getId()).remove(servant);
        gd.playerGraveyards.get(player1.getId()).add(servant.getCard());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
