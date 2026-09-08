package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VexingSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep discards one card per age counter")
    void payingCumulativeUpkeepDiscardsCard() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new VexingSphinx());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

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
        harness.setHand(player1, List.of(new GrizzlyBears()));
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
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        int handBeforeSecondUpkeep = gd.playerHands.get(player1.getId()).size();
        int deckBeforeSecondUpkeep = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sphinx.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphinx);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeSecondUpkeep + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBeforeSecondUpkeep - 2);
    }
}
