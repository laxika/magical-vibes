package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElspethResplendent;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabarettiAscendancy.class, CivicGardener.class, ElspethResplendent.class, Strangle.class})
class CabarettiAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting a creature card reveals it and puts it into hand")
    void acceptsCreatureCard() {
        Card creature = new CivicGardener();
        Card below = new Strangle();
        triggerWithLibrary(creature, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Accepting a planeswalker card reveals it and puts it into hand")
    void acceptsPlaneswalkerCard() {
        Card planeswalker = new ElspethResplendent();
        Card below = new CivicGardener();
        triggerWithLibrary(planeswalker, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Declining the reveal offers the matching card for the bottom of the library")
    void declinesRevealOffersBottomChoice() {
        Card creature = new CivicGardener();
        Card below = new Strangle();
        triggerWithLibrary(creature, below);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, creature);
    }

    @Test
    @DisplayName("Declining both choices leaves a matching card on top")
    void declinesRevealAndBottom() {
        Card creature = new CivicGardener();
        Card below = new Strangle();
        triggerWithLibrary(creature, below);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, below);
    }

    @Test
    @DisplayName("A nonmatching card can be put on the bottom without a reveal choice")
    void nonmatchingCardCanGoToBottom() {
        Card nonmatching = new Strangle();
        Card below = new CivicGardener();
        triggerWithLibrary(nonmatching, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonmatching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, nonmatching);
    }

    @Test
    @DisplayName("Declining the bottom choice leaves a nonmatching card on top")
    void nonmatchingCardCanStayOnTop() {
        Card nonmatching = new Strangle();
        Card below = new CivicGardener();
        triggerWithLibrary(nonmatching, below);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonmatching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching, below);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library resolves without offering a choice or drawing a card")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new CabarettiAscendancy());
        harness.setLibrary(player1, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.passBothPriorities());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The enchantment does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Card creature = new CivicGardener();
        Card below = new Strangle();
        harness.addToBattlefield(player1, new CabarettiAscendancy());
        harness.setLibrary(player1, List.of(creature, below));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, below);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    private void triggerWithLibrary(Card topCard, Card belowTop) {
        harness.addToBattlefield(player1, new CabarettiAscendancy());
        harness.setLibrary(player1, List.of(topCard, belowTop));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
