package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoltaicServantTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct controller end-step triggered effect")
    void hasCorrectEffect() {
        VoltaicServant card = new VoltaicServant();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(UntapTargetPermanentEffect.class);
        UntapTargetPermanentEffect effect = (UntapTargetPermanentEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(effect.targetPredicate()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== Untapping artifacts at end step =====

    @Test
    @DisplayName("Untaps a tapped artifact at controller's end step")
    void untapsTappedArtifactAtEndStep() {
        harness.addToBattlefield(player1, new VoltaicServant());
        harness.addToBattlefield(player1, new AngelsFeather());
        UUID featherId = harness.getPermanentId(player1, "Angel's Feather");
        Permanent feather = gd.playerBattlefields.get(player1.getId()).get(1);
        feather.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step → triggers end step ability
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // Should be awaiting target selection for the artifact
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the tapped Angel's Feather
        harness.handlePermanentChosen(player1, featherId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Feather should be untapped
        assertThat(feather.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap an opponent's artifact")
    void canUntapOpponentArtifact() {
        harness.addToBattlefield(player1, new VoltaicServant());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID featherId = harness.getPermanentId(player2, "Angel's Feather");
        Permanent feather = gd.playerBattlefields.get(player2.getId()).get(0);
        feather.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, featherId);
        harness.passBothPriorities();

        assertThat(feather.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target itself (Voltaic Servant is an artifact creature)")
    void canTargetItself() {
        harness.addToBattlefield(player1, new VoltaicServant());
        UUID servantId = harness.getPermanentId(player1, "Voltaic Servant");
        Permanent servant = gd.playerBattlefields.get(player1.getId()).get(0);
        servant.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, servantId);
        harness.passBothPriorities();

        assertThat(servant.isTapped()).isFalse();
    }

    // ===== Does not trigger on opponent's turn =====

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new VoltaicServant());
        harness.addToBattlefield(player1, new AngelsFeather());
        Permanent feather = gd.playerBattlefields.get(player1.getId()).get(1);
        feather.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        // Advance to end step on opponent's turn
        gs.advanceStep(gd);

        // No trigger should fire for player1's Voltaic Servant
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        // Feather should remain tapped
        assertThat(feather.isTapped()).isTrue();
    }

    // ===== Cannot target non-artifacts =====

    @Test
    @DisplayName("Non-artifact creatures are not valid targets")
    void nonArtifactCreatureNotValidTarget() {
        harness.addToBattlefield(player1, new VoltaicServant());
        // Only non-artifact creature on the battlefield besides Voltaic Servant
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // Should be awaiting target selection — only Voltaic Servant itself should be valid
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Grizzly Bears (non-artifact) should NOT be in valid choices
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThat(gd.interaction.permanentChoice().validIds()).doesNotContain(bearsId);

        // Voltaic Servant (artifact creature) should be a valid target
        UUID servantId = harness.getPermanentId(player1, "Voltaic Servant");
        assertThat(gd.interaction.permanentChoice().validIds()).contains(servantId);
    }

    // ===== Untapping already untapped artifact =====

    @Test
    @DisplayName("Can target an already untapped artifact (no-op untap)")
    void canTargetAlreadyUntappedArtifact() {
        harness.addToBattlefield(player1, new VoltaicServant());
        harness.addToBattlefield(player1, new AngelsFeather());
        UUID featherId = harness.getPermanentId(player1, "Angel's Feather");
        Permanent feather = gd.playerBattlefields.get(player1.getId()).get(1);

        // Feather is already untapped
        assertThat(feather.isTapped()).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, featherId);
        harness.passBothPriorities();

        // Feather remains untapped
        assertThat(feather.isTapped()).isFalse();
    }
}
