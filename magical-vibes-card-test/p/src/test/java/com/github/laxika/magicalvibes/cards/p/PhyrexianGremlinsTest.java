package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianGremlins.class, Ornithopter.class})
class PhyrexianGremlinsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps the target artifact")
    void resolvingTapsTargetArtifact() {
        addReadyGremlins(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Phyrexian Gremlins")
    void activatingTapsGremlins() {
        Permanent gremlins = addReadyGremlins(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(gremlins.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addReadyGremlins(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PhyrexianGremlins());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("The target artifact stays tapped while Phyrexian Gremlins remains tapped")
    void targetArtifactStaysTappedWhileGremlinsTapped() {
        addReadyGremlins(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target artifact untaps after Phyrexian Gremlins untaps")
    void targetArtifactUntapsAfterGremlinsUntap() {
        Permanent gremlins = addReadyGremlins(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);
        harness.performUntapStep(player2);

        assertThat(gremlins.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing not to untap keeps the artifact locked")
    void mayKeepGremlinsTapped() {
        Permanent gremlins = addReadyGremlins(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, false);
        harness.performUntapStep(player2);

        assertThat(gremlins.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped artifact can be targeted and locked")
    void locksAlreadyTappedArtifact() {
        addReadyGremlins(player1);
        Permanent artifact = addReadyArtifact(player2);
        artifact.tap();

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability still taps its target if Gremlins leaves before resolution, but does not lock it")
    void sourceLeavesBeforeResolution() {
        Permanent gremlins = addReadyGremlins(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, gremlins));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Removing Gremlins releases an established untap lock")
    void sourceLeavesAfterResolution() {
        Permanent gremlins = addReadyGremlins(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, gremlins));

        assertThat(artifact.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(artifact.isTapped()).isFalse();
    }

    private Permanent addReadyGremlins(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PhyrexianGremlins());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Ornithopter());
        perm.setSummoningSick(false);
        return perm;
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
