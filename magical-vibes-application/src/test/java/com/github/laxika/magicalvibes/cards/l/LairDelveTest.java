package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LairDelve.class, TimberlandGuide.class, Forest.class, Island.class, PillarOfFlame.class})
class LairDelveTest extends BaseCardTest {

    @Test
    @DisplayName("Creature and land cards revealed go to hand")
    void creatureAndLandGoToHand() {
        Card creature = new TimberlandGuide();
        Card forest = new Forest();
        Card island = new Island();

        harness.setLibrary(player1, List.of(creature, forest, island));

        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("Non-creature non-land cards go to the bottom of the library")
    void othersGoToBottom() {
        Card spell = new PillarOfFlame();
        Card forest = new Forest();
        Card island = new Island();

        harness.setLibrary(player1, List.of(spell, forest, island));

        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest).doesNotContain(spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, spell);
    }

    @Test
    @DisplayName("Only two cards are revealed")
    void onlyTwoCardsRevealed() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card forest3 = new Forest();

        harness.setLibrary(player1, List.of(forest1, forest2, forest3));

        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest1, forest2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest3);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void nonmatchingCardsCanBeOrderedOnBottom() {
        Card first = new PillarOfFlame();
        Card second = new LairDelve();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(first, second, untouched));
        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, second, first);
    }

    @Test
    void singleCreatureInLibraryGoesToHand() {
        Card creature = new TimberlandGuide();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void singleNonmatchingCardStaysInLibrary() {
        Card spell = new PillarOfFlame();
        harness.setLibrary(player1, List.of(spell));
        harness.setHand(player1, List.of(new LairDelve()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
    }
}
