package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodgiftDemonTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Bloodgift Demon has correct upkeep trigger effects")
    void hasCorrectEffects() {
        BloodgiftDemon card = new BloodgiftDemon();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).get(0))
                .isInstanceOf(DrawCardForTargetPlayerEffect.class);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).get(1))
                .isInstanceOf(TargetPlayerLosesLifeEffect.class);
    }

    // ===== Targeting self =====

    @Test
    @DisplayName("Controller targets self: draws a card and loses 1 life")
    void targetSelfDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        // Choose player1 as target
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Targeting opponent =====

    @Test
    @DisplayName("Controller targets opponent: opponent draws a card and loses 1 life")
    void targetOpponentDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        // Choose player2 as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Does not trigger during opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Controller's life is unchanged when targeting opponent =====

    @Test
    @DisplayName("Controller's life is unchanged when targeting opponent")
    void controllersLifeUnchangedWhenTargetingOpponent() {
        harness.addToBattlefield(player1, new BloodgiftDemon());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
