package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PainfulQuandaryTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has spell-cast triggered LoseLifeUnlessDiscardEffect with 5 life loss")
    void hasCorrectEffect() {
        PainfulQuandary card = new PainfulQuandary();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL).getFirst())
                .isInstanceOf(LoseLifeUnlessDiscardEffect.class);
        LoseLifeUnlessDiscardEffect effect =
                (LoseLifeUnlessDiscardEffect) card.getEffects(EffectSlot.ON_OPPONENT_CASTS_SPELL).getFirst();
        assertThat(effect.lifeLoss()).isEqualTo(5);
    }

    // ===== Trigger only on opponent's spells =====

    @Test
    @DisplayName("Does NOT trigger when controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // No triggered ability — only the creature spell on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
    }

    @Test
    @DisplayName("Triggers when opponent casts a spell — puts triggered ability on stack")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        // Triggered ability should be on the stack (on top of the creature spell)
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Painful Quandary");
    }

    // ===== Opponent has cards: chooses to discard =====

    @Test
    @DisplayName("Opponent with cards in hand is prompted to discard or lose life")
    void opponentWithCardsIsPrompted() {
        setupOpponentCastsSpellWithCardsInHand();

        // Resolve the triggered ability — should prompt the opponent
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent accepts discard — prompted to choose card to discard")
    void opponentAcceptsDiscardPrompted() {
        setupOpponentCastsSpellWithCardsInHand();
        harness.passBothPriorities(); // resolve triggered ability

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent discards a card — no life loss")
    void opponentDiscardsNoLifeLoss() {
        setupOpponentCastsSpellWithCardsInHand();
        harness.passBothPriorities(); // resolve triggered ability

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0); // discard the first card

        // No life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);

        // The card was discarded
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Suntail Hawk"));
    }

    // ===== Opponent has cards: declines discard =====

    @Test
    @DisplayName("Opponent declines discard — loses 5 life")
    void opponentDeclinesDiscardLosesLife() {
        setupOpponentCastsSpellWithCardsInHand();
        harness.passBothPriorities(); // resolve triggered ability

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Opponent declines discard — hand is untouched")
    void opponentDeclinesDiscardHandUntouched() {
        setupOpponentCastsSpellWithCardsInHand();
        harness.passBothPriorities(); // resolve triggered ability

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    // ===== Opponent has no cards: auto life loss =====

    @Test
    @DisplayName("Auto-loses 5 life when opponent has empty hand")
    void autoLosesLifeWithEmptyHand() {
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        // Hand is now empty (creature was cast from hand)
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Resolve the triggered ability — auto life loss since hand is empty
        harness.passBothPriorities();

        // No prompt — it was automatic
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard")
                && log.contains("loses 5 life"));
    }

    // ===== Multiple Painful Quandaries =====

    @Test
    @DisplayName("Multiple Painful Quandaries each trigger independently")
    void multipleTriggerIndependently() {
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears(), new SuntailHawk(), new SuntailHawk()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve first triggered ability — opponent discards
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0); // discard first card

        // Resolve second triggered ability — opponent declines
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        // Lost 5 life from declining the second trigger
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);

        // One card was discarded
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    // ===== Life loss can kill opponent =====

    @Test
    @DisplayName("Life loss from declining can reduce opponent to 0 or below")
    void lifeLossCanKill() {
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player2, 3);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        // Hand is empty — auto life loss
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-2);
    }

    // ===== Helpers =====

    /**
     * Sets up: player1 has Painful Quandary on battlefield, player2 casts a spell
     * with additional cards remaining in hand. After this, the triggered ability
     * is on the stack (not yet resolved).
     */
    private void setupOpponentCastsSpellWithCardsInHand() {
        harness.addToBattlefield(player1, new PainfulQuandary());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears(), new SuntailHawk()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        // Sanity check: triggered ability is on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }
}
