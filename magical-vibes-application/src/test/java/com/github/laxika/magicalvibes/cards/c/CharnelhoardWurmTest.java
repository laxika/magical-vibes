package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CharnelhoardWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage lets the controller return a chosen graveyard card to hand")
    void dealingDamageReturnsChosenCardToHand() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setLife(player2, 20);
        attackWithWurmDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Declining the return (choosing no card) leaves the graveyard card in place")
    void decliningLeavesCardInGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setLife(player2, 20);
        attackWithWurmDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("With an empty graveyard the trigger prompts nothing and damage is still dealt")
    void emptyGraveyardDealsDamageWithoutPrompt() {
        harness.setGraveyard(player1, List.of());
        harness.setLife(player2, 20);
        attackWithWurmDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private void attackWithWurmDealingDamage() {
        Permanent wurm = addCreatureReady(player1, new CharnelhoardWurm());
        wurm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of()); // no blockers — the 6/6 hits the player
        harness.passBothPriorities(); // advance to combat damage; the damage trigger fires
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
