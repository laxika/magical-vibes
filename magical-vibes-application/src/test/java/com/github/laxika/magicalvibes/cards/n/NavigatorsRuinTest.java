package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NavigatorsRuinTest extends BaseCardTest {

    // ===== Raid met — mills target opponent =====

    @Test
    @DisplayName("When raid met, target opponent mills 4 cards")
    void raidMetMillsOpponent() {
        harness.addToBattlefield(player1, new NavigatorsRuin());

        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step — raid trigger fires, needs target selection
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Select opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Triggered ability is now on the stack — resolve it
        harness.passBothPriorities();

        // Opponent should have milled 4 cards into graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()).size())
                .isGreaterThanOrEqualTo(graveyardBefore + 4);
    }

    // ===== Raid not met — no trigger =====

    @Test
    @DisplayName("When raid not met (did not attack), end step trigger does not fire")
    void raidNotMetNoTrigger() {
        harness.addToBattlefield(player1, new NavigatorsRuin());

        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        // Do NOT mark attacked this turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // No mill — raid condition not met; graveyard should be unchanged
        assertThat(gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(graveyardBefore);
    }

    // ===== Does not trigger on opponent's end step =====

    @Test
    @DisplayName("Does not trigger on opponent's end step even if controller attacked")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new NavigatorsRuin());

        // Mark player1 attacked, but it's player2's turn
        markAttackedThisTurn();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // No trigger for player1's Navigator's Ruin on player2's end step
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
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
