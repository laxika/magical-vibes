package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrightglassGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB search only shows qualifying artifact, creature, and enchantment cards")
    void searchOnlyShowsQualifyingCards() {
        setupAndCast();
        Memnite memnite = new Memnite();
        LlanowarElves elves = new LlanowarElves();
        WildGrowth wildGrowth = new WildGrowth();
        setLibrary(memnite, elves, wildGrowth, new GrizzlyBears());

        resolveMayAbility(true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactlyInAnyOrder(memnite, elves, wildGrowth);
    }

    @Test
    @DisplayName("The ETB search puts at most two selected cards into hand")
    void searchPutsAtMostTwoCardsIntoHand() {
        setupAndCast();
        Memnite memnite = new Memnite();
        LlanowarElves elves = new LlanowarElves();
        WildGrowth wildGrowth = new WildGrowth();
        setLibrary(memnite, elves, wildGrowth);

        resolveMayAbility(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).contains(wildGrowth);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining the ETB search leaves the library and hand unchanged")
    void decliningSearchDoesNothing() {
        setupAndCast();
        List<Card> library = List.of(new Memnite(), new GrizzlyBears());
        setLibrary(library.toArray(Card[]::new));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveMayAbility(false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new BrightglassGearhulk()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveMayAbility(boolean choice) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, choice);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
