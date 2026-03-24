package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaraudingLooterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has RaidConditionalEffect wrapping MayEffect(DrawAndDiscardCardEffect) on CONTROLLER_END_STEP_TRIGGERED")
    void hasCorrectEffect() {
        MaraudingLooter card = new MaraudingLooter();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(RaidConditionalEffect.class);
        RaidConditionalEffect raid =
                (RaidConditionalEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(raid.wrapped()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) raid.wrapped();
        assertThat(may.wrapped()).isInstanceOf(DrawAndDiscardCardEffect.class);
    }

    // ===== Raid met — accept may =====

    @Test
    @DisplayName("When raid met and may accepted, draws a card then discards a card")
    void raidMetAcceptMayDrawsAndDiscards() {
        harness.addToBattlefield(player1, new MaraudingLooter());
        setDeck(player1, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Advance to end step — raid trigger queues MayEffect onto stack
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        // Resolve the triggered ability — MayEffect presents the may choice
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Drew a card, now awaiting discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        // Discard a card
        harness.handleCardChosen(player1, 0);

        // Drew 1, discarded 1 — net hand size unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Raid met — decline may =====

    @Test
    @DisplayName("When raid met and may declined, no draw or discard occurs")
    void raidMetDeclineMayNoDrawNoDiscard() {
        harness.addToBattlefield(player1, new MaraudingLooter());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Advance to end step — raid trigger queues MayEffect onto stack
        harness.passBothPriorities();

        // Resolve the triggered ability — MayEffect presents the may choice
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // Hand size unchanged — no draw, no discard
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Raid not met — no trigger =====

    @Test
    @DisplayName("When raid not met (did not attack), end step trigger does not fire")
    void raidNotMetNoTrigger() {
        harness.addToBattlefield(player1, new MaraudingLooter());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        // Do NOT mark attacked this turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        // No may ability prompt — raid condition not met
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();

        // Hand size unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    // ===== Does not trigger on opponent's end step =====

    @Test
    @DisplayName("Does not trigger on opponent's end step even if controller attacked")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new MaraudingLooter());

        // Mark player1 attacked, but it's player2's turn
        markAttackedThisTurn();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // No trigger for player1's Marauding Looter on player2's end step
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
