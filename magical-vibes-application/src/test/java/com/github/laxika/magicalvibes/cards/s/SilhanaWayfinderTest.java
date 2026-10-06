package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.cards.g.GrowthSpiral;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilhanaWayfinder.class, GrowthSpiral.class, GruulGuildgate.class})
class SilhanaWayfinderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a creature or land from the top four")
    void etbOffersCreatureOrLand() {
        Card creature = new SilhanaWayfinder();
        Card land = new GruulGuildgate();
        harness.setLibrary(player1, List.of(creature, new GrowthSpiral(), land, new GrowthSpiral()));
        castWayfinder();

        PendingInteraction.LibrarySearch search = resolveEtb();

        assertThat(search.params().cards()).containsExactly(creature, land);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature or land puts it on top and randomizes the rest on the bottom")
    void chosenCardGoesOnTop() {
        Card creature = new SilhanaWayfinder();
        Card land = new GruulGuildgate();
        Card spell = new GrowthSpiral();
        Card otherSpell = new GrowthSpiral();
        harness.setLibrary(player1, List.of(creature, land, spell, otherSpell));
        castWayfinder();

        PendingInteraction.LibrarySearch search = resolveEtb();
        int landIndex = search.params().cards().indexOf(land);
        harness.handleCardChosen(player1, landIndex);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.getFirst()).isSameAs(land);
        assertThat(library.subList(1, library.size())).containsExactlyInAnyOrder(creature, spell, otherSpell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may pick puts all four cards on the bottom")
    void mayPickCanBeDeclined() {
        Card creature = new SilhanaWayfinder();
        Card land = new GruulGuildgate();
        Card spell = new GrowthSpiral();
        Card otherSpell = new GrowthSpiral();
        harness.setLibrary(player1, List.of(creature, land, spell, otherSpell));
        castWayfinder();

        resolveEtb();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, land, spell, otherSpell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing a creature preserves the unseen library and bottoms only the other looked-at cards")
    void creatureChoicePreservesUnseenLibrary() {
        Card creature = new SilhanaWayfinder();
        Card land = new GruulGuildgate();
        Card spell = new GrowthSpiral();
        Card otherSpell = new GrowthSpiral();
        Card unseenLand = new GruulGuildgate();
        Card unseenCreature = new SilhanaWayfinder();
        harness.setLibrary(player1, List.of(creature, land, spell, otherSpell, unseenLand, unseenCreature));
        castWayfinder();

        PendingInteraction.LibrarySearch search = resolveEtb();
        assertThat(search.params().cards()).containsExactly(creature, land);
        harness.handleCardChosen(player1, search.params().cards().indexOf(creature));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.subList(0, 3)).containsExactly(creature, unseenLand, unseenCreature);
        assertThat(library.subList(3, 6)).containsExactlyInAnyOrder(land, spell, otherSpell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining leaves unseen cards above all four looked-at cards")
    void decliningPreservesUnseenLibrary() {
        Card creature = new SilhanaWayfinder();
        Card land = new GruulGuildgate();
        Card spell = new GrowthSpiral();
        Card otherSpell = new GrowthSpiral();
        Card unseen = new GruulGuildgate();
        harness.setLibrary(player1, List.of(creature, land, spell, otherSpell, unseen));
        castWayfinder();

        resolveEtb();
        harness.handleCardChosen(player1, -1);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(5);
        assertThat(library.getFirst()).isSameAs(unseen);
        assertThat(library.subList(1, 5)).containsExactlyInAnyOrder(creature, land, spell, otherSpell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No eligible card among the top four bottoms them without offering the fifth card")
    void noEligibleCards() {
        Card first = new GrowthSpiral();
        Card second = new GrowthSpiral();
        Card third = new GrowthSpiral();
        Card fourth = new GrowthSpiral();
        Card unseen = new SilhanaWayfinder();
        harness.setLibrary(player1, List.of(first, second, third, fourth, unseen));
        castWayfinder();

        resolveEtb();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(5);
        assertThat(library.getFirst()).isSameAs(unseen);
        assertThat(library.subList(1, 5)).containsExactlyInAnyOrder(first, second, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than four cards uses only the available cards")
    void shortLibrary() {
        Card spell = new GrowthSpiral();
        Card land = new GruulGuildgate();
        harness.setLibrary(player1, List.of(spell, land));
        castWayfinder();

        PendingInteraction.LibrarySearch search = resolveEtb();
        assertThat(search.params().cards()).containsExactly(land);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The only card in a library can still be declined")
    void singleEligibleCardCanBeDeclined() {
        Card land = new GruulGuildgate();
        harness.setLibrary(player1, List.of(land));
        castWayfinder();

        PendingInteraction.LibrarySearch search = resolveEtb();
        assertThat(search.params().cards()).containsExactly(land);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library completes the ability without a choice")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        castWayfinder();

        resolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castWayfinder() {
        harness.setHand(player1, List.of(new SilhanaWayfinder()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private PendingInteraction.LibrarySearch resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }
}
