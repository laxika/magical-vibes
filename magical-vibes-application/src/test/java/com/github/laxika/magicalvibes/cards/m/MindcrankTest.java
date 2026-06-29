package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindcrankTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Mindcrank has correct triggered effect")
    void hasCorrectProperties() {
        Mindcrank card = new Mindcrank();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_LOSES_LIFE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_LOSES_LIFE).getFirst())
                .isInstanceOf(MillOpponentOnLifeLossEffect.class);
    }

    // ===== Trigger on spell damage =====

    @Test
    @DisplayName("Mindcrank mills opponent when they take spell damage")
    void millsOnSpellDamage() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();

        // Shock player2 for 2 damage
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player2 lost 2 life → mills 2 cards
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSizeBefore + 2);
    }

    // ===== Trigger on combat damage =====

    @Test
    @DisplayName("Mindcrank mills opponent when they take combat damage")
    void millsOnCombatDamage() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        // Put an attacking 2/2 creature on player1's battlefield
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player2 took 2 combat damage → mills 2 cards
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
    }

    // ===== No trigger on controller's own life loss =====

    @Test
    @DisplayName("Mindcrank does NOT trigger when its controller loses life")
    void doesNotTriggerOnControllerLifeLoss() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player1, 20);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Player1 shocks themselves for 2 damage
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Player1 lost life but Mindcrank shouldn't trigger (it's on player1's side)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    // ===== Two Mindcranks each trigger =====

    @Test
    @DisplayName("Two Mindcranks each trigger when opponent loses life, milling twice")
    void twoMindcranksEachTrigger() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 damage → each Mindcrank mills 2 → total 4 milled
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
    }

    // ===== Trigger is logged =====

    @Test
    @DisplayName("Mindcrank trigger is logged")
    void triggerIsLogged() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Mindcrank") && log.contains("triggers") && log.contains("mills"));
    }

    // ===== Mindcrank does not trigger when not on battlefield =====

    @Test
    @DisplayName("Mindcrank does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        harness.setLife(player2, 20);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No Mindcrank on battlefield — no milling
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    // ===== Opponent's Mindcrank triggers on our life loss =====

    @Test
    @DisplayName("Opponent's Mindcrank triggers when we lose life")
    void opponentsMindcrankTriggersOnOurLifeLoss() {
        harness.addToBattlefield(player2, new Mindcrank());
        harness.setLife(player1, 20);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Player1 shocks themselves — player2's Mindcrank should trigger
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Player1 lost 2 life → player2's Mindcrank mills player1 for 2
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    // ===== Mills zero when opponent has empty library =====

    @Test
    @DisplayName("Mindcrank mills nothing when opponent has no library")
    void millsNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new Mindcrank());
        harness.setLife(player2, 20);

        // Empty player2's library
        gd.playerDecks.get(player2.getId()).clear();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Damage still happens, milling just doesn't do anything
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
