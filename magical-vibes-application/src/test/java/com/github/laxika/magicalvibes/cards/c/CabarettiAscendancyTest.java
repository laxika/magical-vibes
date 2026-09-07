package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceWielderOfMysteries;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabarettiAscendancy.class, GrizzlyBears.class, JaceWielderOfMysteries.class, Shock.class})
class CabarettiAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting a creature card reveals it and puts it into hand")
    void acceptsCreatureCard() {
        Card creature = new GrizzlyBears();
        Card below = new Shock();
        triggerWithLibrary(creature, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Accepting a planeswalker card reveals it and puts it into hand")
    void acceptsPlaneswalkerCard() {
        Card planeswalker = new JaceWielderOfMysteries();
        Card below = new GrizzlyBears();
        triggerWithLibrary(planeswalker, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Declining the reveal offers the matching card for the bottom of the library")
    void declinesRevealOffersBottomChoice() {
        Card creature = new GrizzlyBears();
        Card below = new Shock();
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
        Card creature = new GrizzlyBears();
        Card below = new Shock();
        triggerWithLibrary(creature, below);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, below);
    }

    @Test
    @DisplayName("A nonmatching card can be put on the bottom without a reveal choice")
    void nonmatchingCardCanGoToBottom() {
        Card nonmatching = new Shock();
        Card below = new GrizzlyBears();
        triggerWithLibrary(nonmatching, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonmatching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, nonmatching);
    }

    private void triggerWithLibrary(Card topCard, Card belowTop) {
        harness.addToBattlefield(player1, new CabarettiAscendancy());
        harness.setLibrary(player1, List.of(topCard, belowTop));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
