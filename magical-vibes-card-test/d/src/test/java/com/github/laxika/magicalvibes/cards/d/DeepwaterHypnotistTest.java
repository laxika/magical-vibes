package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepwaterHypnotist.class, GrizzlyBears.class})
class DeepwaterHypnotistTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Deepwater Hypnotist queues a target creature choice")
    void untappingQueuesTargetChoice() {
        Permanent hypnotist = harness.addToBattlefieldAndReturn(player1, new DeepwaterHypnotist());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        hypnotist.tap();

        runUntapStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validIds())
                .contains(bears.getId());
    }

    @Test
    @DisplayName("The chosen opponent creature gets -3/-0 until end of turn")
    void appliesMinusThreeZeroToOpponentCreature() {
        Permanent hypnotist = harness.addToBattlefieldAndReturn(player1, new DeepwaterHypnotist());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        hypnotist.tap();

        runUntapStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-3);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(-1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The -3/-0 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent hypnotist = harness.addToBattlefieldAndReturn(player1, new DeepwaterHypnotist());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        hypnotist.tap();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        runUntapStep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The trigger cannot target a creature controlled by its controller")
    void cannotTargetYourOwnCreature() {
        Permanent hypnotist = harness.addToBattlefieldAndReturn(player1, new DeepwaterHypnotist());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        hypnotist.tap();

        runUntapStep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The trigger is skipped when no opponent creature is available")
    void skippedWhenNoOpponentCreature() {
        Permanent hypnotist = harness.addToBattlefieldAndReturn(player1, new DeepwaterHypnotist());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        hypnotist.tap();

        runUntapStep(player1);

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)).isFalse();
    }

    @Test
    @DisplayName("An already untapped Hypnotist does not trigger during the untap step")
    void alreadyUntappedDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeepwaterHypnotist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeepwaterHypnotist());

        runUntapStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The untap trigger resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent hypnotist = harness.addToBattlefieldAndReturn(player1, new DeepwaterHypnotist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeepwaterHypnotist());
        hypnotist.tap();

        runUntapStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(hypnotist);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A target that leaves before resolution receives no modifier")
    void targetLeavesBeforeResolution() {
        Permanent hypnotist = harness.addToBattlefieldAndReturn(player1, new DeepwaterHypnotist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeepwaterHypnotist());
        hypnotist.tap();

        runUntapStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(untappingPlayer, TurnStep.UPKEEP);
        harness.passPriority(untappingPlayer);
    }
}
