package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MishrasWarMachineTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    private Permanent machine(Player owner) {
        UUID id = harness.getPermanentId(owner, "Mishra's War Machine");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Declining the discard deals 3 damage to its controller and taps it")
    void declineDealsDamageAndTaps() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(machine(player1).isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
        // Card was not discarded
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Accepting discards a card with no damage and no tap")
    void acceptDiscardsCardNoPenalty() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(machine(player1).isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With no cards in hand, the penalty applies immediately without a prompt")
    void noCardsAppliesPenaltyImmediately() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → penalty (no card to discard)

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(machine(player1).isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new MishrasWarMachine());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(machine(player1).isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
