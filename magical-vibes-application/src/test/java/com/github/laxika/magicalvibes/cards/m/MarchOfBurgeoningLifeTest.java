package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfBurgeoningLife.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Card.class})
class MarchOfBurgeoningLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a same-named creature and puts it onto the battlefield tapped")
    void searchesForSameNamedCreatureTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card sameNamedSorcery = new Card();
        sameNamedSorcery.setName("Grizzly Bears");
        sameNamedSorcery.setType(CardType.SORCERY);
        harness.setLibrary(player1, List.of(sameNamedSorcery, new GrizzlyBears()));

        harness.setHand(player1, List.of(new MarchOfBurgeoningLife()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorceryWithDiscards(player1, 0, 3, target.getId(), List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears")).filteredOn(Permanent::isTapped).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiling a green card reduces the generic cost by two")
    void exilingGreenCardReducesGenericCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfBurgeoningLife(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorceryWithDiscards(player1, 0, 3, target.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The optional hand exile cost only accepts green cards")
    void handExileCostRequiresGreenCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfBurgeoningLife(), new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(
                player1, 0, 3, target.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    @DisplayName("A target with mana value equal to X is illegal")
    void rejectsTargetEqualToX() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfBurgeoningLife()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        assertThatThrownBy(() -> harness.castSorceryWithDiscards(
                player1, 0, 2, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void canTargetCreatureCopyToken() {
        Card token = new GrizzlyBears();
        token.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, token);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MarchOfBurgeoningLife()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorceryWithDiscards(player1, 0, 3, target.getId(), List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears")).filteredOn(Permanent::isTapped).hasSize(1);
    }

    @Test
    void excessExiledCardsDoNotReduceGreenManaRequirementOrChosenX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MarchOfBurgeoningLife(), new LlanowarElves(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorceryWithDiscards(player1, 0, 3, target.getId(), List.of(1, 2));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(findPermanents(player1, "Grizzly Bears")).singleElement().matches(Permanent::isTapped);
        assertThat(findPermanents(player2, "Grizzly Bears")).containsExactly(target);
    }

    @Test
    void mayFailToFindEvenWhenMatchingCreatureExists() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new MarchOfBurgeoningLife()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorceryWithDiscards(player1, 0, 3, target.getId(), List.of());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
