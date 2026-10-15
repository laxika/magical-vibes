package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VexingSphinx.class, MishrasBauble.class})
class VexingSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep discards one card per age counter")
    void payingCumulativeUpkeepDiscardsCard() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new VexingSphinx());
        harness.setHand(player1, List.of(new MishrasBauble(), new MishrasBauble()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(sphinx.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sphinx);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Vexing Sphinx and draws for its age counters")
    void decliningCumulativeUpkeepDrawsForAgeCounters() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new VexingSphinx());
        harness.setHand(player1, List.of(new MishrasBauble()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphinx);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Insufficient cards on a later upkeep sacrifices Vexing Sphinx and draws twice")
    void insufficientCardsOnLaterUpkeepSacrificesIt() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new VexingSphinx());
        harness.setHand(player1, List.of(new MishrasBauble(), new MishrasBauble()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        int handBeforeSecondUpkeep = gd.playerHands.get(player1.getId()).size();
        int deckBeforeSecondUpkeep = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(sphinx.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphinx);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeSecondUpkeep + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeSecondUpkeep - 2);
    }

    @Test
    @DisplayName("Paying a later cumulative upkeep discards one card per age counter")
    void payingLaterCumulativeUpkeepDiscardsForEachAgeCounter() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new VexingSphinx());
        sphinx.setCounterCount(CounterType.AGE, 1);
        harness.setHand(player1, List.of(
                new MishrasBauble(), new MishrasBauble(), new MishrasBauble()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(sphinx.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sphinx);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Death draws only for age counters and only for the dying creature's controller")
    void deathDrawsOnlyForAgeCounters() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new VexingSphinx());
        sphinx.setCounterCount(CounterType.AGE, 3);
        sphinx.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        sphinx.setMarkedDamage(6);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Vexing Sphinx");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Dying without age counters draws no cards")
    void deathWithoutAgeCountersDrawsNothing() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new VexingSphinx());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        sphinx.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Vexing Sphinx");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("An empty hand prevents paying the first upkeep but the new age counter still draws a card")
    void emptyHandOnFirstUpkeepSacrificesAndDraws() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new VexingSphinx());
        harness.setHand(player1, List.of());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphinx);
        harness.assertInGraveyard(player1, "Vexing Sphinx");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Cumulative upkeep does not trigger during the opponent's upkeep")
    void opponentUpkeepDoesNotAddAgeCounter() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new VexingSphinx());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(sphinx.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sphinx);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
