package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilhanaWayfinderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a creature or land from the top four")
    void etbOffersCreatureOrLand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, new Shock(), land, new Shock()));
        castWayfinder();

        PendingInteraction.LibrarySearch search = resolveEtb();

        assertThat(search.params().cards()).containsExactly(creature, land);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature or land puts it on top and randomizes the rest on the bottom")
    void chosenCardGoesOnTop() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        Card otherSpell = new Shock();
        harness.setLibrary(player1, List.of(creature, land, spell, otherSpell));
        castWayfinder();

        PendingInteraction.LibrarySearch search = resolveEtb();
        int landIndex = search.params().cards().indexOf(land);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(landIndex));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.getFirst()).isSameAs(land);
        assertThat(library.subList(1, library.size())).containsExactlyInAnyOrder(creature, spell, otherSpell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may pick puts all four cards on the bottom")
    void mayPickCanBeDeclined() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new Shock();
        Card otherSpell = new Shock();
        harness.setLibrary(player1, List.of(creature, land, spell, otherSpell));
        castWayfinder();

        resolveEtb();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, land, spell, otherSpell);
        assertThat(gd.interaction.activeInteraction()).isNull();
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
