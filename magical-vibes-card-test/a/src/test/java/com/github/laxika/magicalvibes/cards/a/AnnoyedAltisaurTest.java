package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnnoyedAltisaur.class, GrizzlyBears.class})
class AnnoyedAltisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade offers a lower-mana-value card and casts it for free")
    void cascadeCastsLowerManaValueCardForFree() {
        harness.setHand(player1, List.of(new AnnoyedAltisaur()));
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(hit));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");

        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    void cascadeSkipsEqualManaValueAndStopsAtFirstHit() {
        AnnoyedAltisaur skipped = new AnnoyedAltisaur();
        GrizzlyBears hit = new GrizzlyBears();
        GrizzlyBears untouched = new GrizzlyBears();
        harness.setHand(player1, List.of(new AnnoyedAltisaur()));
        harness.setLibrary(player1, List.of(skipped, hit, untouched));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, skipped);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(findPermanent(player1, "Annoyed Altisaur")).isNotNull();
    }

    @Test
    void decliningCascadeBottomsAllExiledCardsAfterUntouchedCards() {
        AnnoyedAltisaur skipped = new AnnoyedAltisaur();
        GrizzlyBears hit = new GrizzlyBears();
        GrizzlyBears untouched = new GrizzlyBears();
        harness.setHand(player1, List.of(new AnnoyedAltisaur()));
        harness.setLibrary(player1, List.of(skipped, hit, untouched));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Annoyed Altisaur")).isNotNull();
    }

    @Test
    void cascadeCardsAreInExileWhileChoosingWhetherToCast() {
        AnnoyedAltisaur skipped = new AnnoyedAltisaur();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setHand(player1, List.of(new AnnoyedAltisaur()));
        harness.setLibrary(player1, List.of(skipped, hit));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(skipped, hit);
    }

    @Test
    void cascadeWithoutQualifyingCardReturnsEntireLibrary() {
        AnnoyedAltisaur first = new AnnoyedAltisaur();
        AnnoyedAltisaur second = new AnnoyedAltisaur();
        harness.setHand(player1, List.of(new AnnoyedAltisaur()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Annoyed Altisaur")).isNotNull();
    }
}
