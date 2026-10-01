package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.PactOfTheTitan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nihilith.class, PactOfTheTitan.class})
class NihilithTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Nihilith with seven time counters")
    void suspendExilesWithSevenTimeCounters() {
        Nihilith card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 7);
    }

    @Test
    @DisplayName("An opponent's card entering their graveyard offers to remove a time counter")
    void opponentCardEnteringGraveyardOffersTimeCounterRemoval() {
        Nihilith card = suspendCard();

        castOpponentPact();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 6);
    }

    @Test
    @DisplayName("Declining Nihilith's graveyard trigger leaves its time counters unchanged")
    void decliningTriggerLeavesTimeCountersUnchanged() {
        Nihilith card = suspendCard();

        castOpponentPact();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 7);
    }

    @Test
    @DisplayName("A card entering its controller's own graveyard does not trigger Nihilith")
    void ownCardEnteringGraveyardDoesNotTrigger() {
        Nihilith card = suspendCard();

        castOwnPact();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 7);
    }

    private Nihilith suspendCard() {
        Nihilith card = new Nihilith();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void castOpponentPact() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new PactOfTheTitan(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castOwnPact() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new PactOfTheTitan(), "{0}");
        harness.passBothPriorities();
    }
}
