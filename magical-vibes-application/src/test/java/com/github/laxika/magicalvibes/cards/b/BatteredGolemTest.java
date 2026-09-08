package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatteredGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Battered Golem does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();

        advanceToNextTurn(player2);

        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An artifact entering triggers the may untap prompt")
    void artifactEnteringTriggersMayPrompt() {
        addReadyGolem(player1);
        castArtifactFor(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting untaps Battered Golem when an artifact enters")
    void acceptUntapsGolem() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        castArtifactFor(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves Battered Golem tapped")
    void declineLeavesGolemTapped() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        castArtifactFor(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control also triggers Battered Golem")
    void opponentArtifactEnteringTriggersMayPrompt() {
        addReadyGolem(player1);
        castArtifactFor(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A non-artifact entering does not trigger Battered Golem")
    void nonArtifactDoesNotTrigger() {
        addReadyGolem(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private Permanent addReadyGolem(Player player) {
        Permanent golem = harness.addToBattlefieldAndReturn(player, new BatteredGolem());
        golem.setSummoningSick(false);
        return golem;
    }

    private void castArtifactFor(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new GlazeFiend()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
