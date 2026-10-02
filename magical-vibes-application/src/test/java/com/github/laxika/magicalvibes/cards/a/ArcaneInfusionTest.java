package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SeizeTheStorm;
import com.github.laxika.magicalvibes.cards.c.ComponentCollector;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneInfusion.class, SeizeTheStorm.class, ComponentCollector.class, Island.class, Consider.class, Swamp.class})
class ArcaneInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Only instant and sorcery cards among the top four are offered")
    void offersOnlyInstantsAndSorceries() {
        Card instant = new Consider();
        Card sorcery = new SeizeTheStorm();
        setupTopCards(List.of(instant, new ComponentCollector(), sorcery, new Island()));
        cast();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The chosen spell goes to hand and the rest go to the bottom")
    void chosenSpellGoesToHandAndRestToBottom() {
        Card instant = new Consider();
        setupTopCards(List.of(instant, new ComponentCollector(), new SeizeTheStorm(), new Island()));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        harness.assertInHand(player1, "Consider");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Component Collector", "Seize the Storm", "Island");
        harness.assertInGraveyard(player1, "Arcane Infusion");
    }

    @Test
    @DisplayName("Declining the reveal puts all four cards on the bottom")
    void mayDecline() {
        setupTopCards(List.of(new Consider(), new ComponentCollector(), new SeizeTheStorm(), new Island()));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Consider", "Component Collector", "Seize the Storm", "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback exiles Arcane Infusion after it resolves")
    void flashbackExilesAfterResolving() {
        Card spell = new ArcaneInfusion();
        setupTopCards(List.of(new ComponentCollector(), new Island(), new Swamp(), new ComponentCollector()));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertNotInGraveyard(player1, "Arcane Infusion");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void unexaminedCardsStayAboveTheRandomizedRemainder() {
        Card sorcery = new SeizeTheStorm();
        Card instant = new Consider();
        Card creature = new ComponentCollector();
        Card land = new Island();
        Card fifth = new Swamp();
        Card sixth = new Consider();
        setupTopCards(List.of(sorcery, instant, creature, land, fifth, sixth));
        cast();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(sorcery.getId(), instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(5);
        assertThat(library.subList(0, 2)).containsExactly(fifth, sixth);
        assertThat(library.subList(2, 5)).containsExactlyInAnyOrder(instant, creature, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryStillAllowsChoosingASpell() {
        Card instant = new Consider();
        Card land = new Island();
        setupTopCards(List.of(instant, land));
        cast();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Arcane Infusion");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        setupTopCards(List.of());
        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Arcane Infusion");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noEligibleCardsReturnToBottomWithoutAChoice() {
        Card creature = new ComponentCollector();
        Card island = new Island();
        Card swamp = new Swamp();
        Card otherCreature = new ComponentCollector();
        Card fifth = new Consider();
        setupTopCards(List.of(creature, island, swamp, otherCreature, fifth));
        cast();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(5);
        assertThat(library.getFirst()).isSameAs(fifth);
        assertThat(library.subList(1, 5)).containsExactlyInAnyOrder(creature, island, swamp, otherCreature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Arcane Infusion");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void flashbackSelectionPutsSpellInHandAndExilesInfusion() {
        Card infusion = new ArcaneInfusion();
        Card sorcery = new SeizeTheStorm();
        setupTopCards(List.of(sorcery));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(infusion));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Arcane Infusion");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(infusion);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast() {
        harness.setHand(player1, List.of(new ArcaneInfusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
