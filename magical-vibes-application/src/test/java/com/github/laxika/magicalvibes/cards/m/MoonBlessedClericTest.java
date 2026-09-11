package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonBlessedCleric.class, Pacifism.class, GrizzlyBears.class})
class MoonBlessedClericTest extends BaseCardTest {

    @Test
    void acceptingMayAbilityOffersOnlyEnchantmentsAndPutsTheChosenCardOnTop() {
        Pacifism pacifism = new Pacifism();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(pacifism, bears);
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).containsExactly(pacifism);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pacifism, bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningMayAbilitySkipsTheSearch() {
        Pacifism pacifism = new Pacifism();
        setLibrary(pacifism);
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pacifism);
    }

    @Test
    void acceptingWithNoEnchantmentDoesNotCreateLibraryInteraction() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears);
        castMoonBlessedCleric();

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    private void castMoonBlessedCleric() {
        harness.setHand(player1, List.of(new MoonBlessedCleric()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
