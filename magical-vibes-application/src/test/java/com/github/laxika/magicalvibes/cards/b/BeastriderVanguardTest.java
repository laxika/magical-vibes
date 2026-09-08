package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BeastriderVanguardTest extends BaseCardTest {

    @Test
    void offersOnlyPermanentCardsFromTopThree() {
        Card permanent = new GrizzlyBears();
        Card instant = new Shock();
        Card land = new Plains();
        setLibrary(permanent, instant, land);
        activateAbility();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(permanent, land);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    void chosenPermanentGoesToHandAndRestIsOrderedOnBottom() {
        Card permanent = new GrizzlyBears();
        Card instant = new Shock();
        Card land = new Plains();
        Card belowTopThree = new LlanowarElves();
        setLibrary(permanent, instant, land, belowTopThree);
        activateAbility();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(permanent);
        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(instant, land);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowTopThree, land, instant);
    }

    @Test
    void mayDeclineAndPutAllLookedAtCardsOnBottom() {
        Card permanent = new GrizzlyBears();
        Card instant = new Shock();
        Card land = new Plains();
        setLibrary(permanent, instant, land);
        activateAbility();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(permanent, instant, land);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(permanent, instant, land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, permanent, instant);
    }

    private void activateAbility() {
        harness.addToBattlefield(player1, new BeastriderVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
