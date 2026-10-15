package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.j.JaceWielderOfMysteries;
import com.github.laxika.magicalvibes.cards.m.ManaGeode;
import com.github.laxika.magicalvibes.cards.n.NewHorizons;
import com.github.laxika.magicalvibes.cards.s.SamutsSprint;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BondOfFlourishing.class, Forest.class, GoblinAssailant.class, SamutsSprint.class,
        JaceWielderOfMysteries.class, NewHorizons.class, ManaGeode.class})
class BondOfFlourishingTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only permanent cards from the top three")
    void offersPermanentCards() {
        GoblinAssailant creature = new GoblinAssailant();
        SamutsSprint instant = new SamutsSprint();
        Forest forest = new Forest();
        setupTopCards(creature, instant, forest);
        cast();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().cards()).containsExactly(creature, forest);
    }

    @Test
    @DisplayName("Puts the chosen permanent in hand, orders the rest on the bottom, and gains 3 life")
    void choosesPermanentOrdersRestAndGainsLife() {
        GoblinAssailant creature = new GoblinAssailant();
        SamutsSprint instant = new SamutsSprint();
        Forest forest = new Forest();
        setupTopCards(creature, instant, forest);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(instant, forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, instant);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("May decline the permanent and still order all three cards on the bottom")
    void mayDeclinePermanent() {
        GoblinAssailant creature = new GoblinAssailant();
        SamutsSprint instant = new SamutsSprint();
        Forest forest = new Forest();
        setupTopCards(creature, instant, forest);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(creature, instant, forest);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, instant, creature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("With no permanent among the top three, orders them on the bottom and gains life")
    void noPermanentCards() {
        SamutsSprint instant1 = new SamutsSprint();
        SamutsSprint instant2 = new SamutsSprint();
        SamutsSprint instant3 = new SamutsSprint();
        setupTopCards(instant1, instant2, instant3);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(instant1, instant2, instant3);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant3, instant1, instant2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Artifact, enchantment, and planeswalker cards are all eligible")
    void offersOtherPermanentTypes() {
        ManaGeode artifact = new ManaGeode();
        NewHorizons enchantment = new NewHorizons();
        JaceWielderOfMysteries planeswalker = new JaceWielderOfMysteries();
        setupTopCards(artifact, enchantment, planeswalker);
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(artifact, enchantment, planeswalker);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(2));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment, artifact);
    }

    @Test
    @DisplayName("Looks only at the top three and puts the rest below untouched cards")
    void preservesUntouchedLibraryCards() {
        GoblinAssailant creature = new GoblinAssailant();
        SamutsSprint instant = new SamutsSprint();
        Forest land = new Forest();
        Forest fourth = new Forest();
        GoblinAssailant fifth = new GoblinAssailant();
        setupTopCards(creature, instant, land, fourth, fifth);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(creature, land);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, fifth, instant, creature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("An empty library still allows the life gain")
    void emptyLibraryStillGainsLife() {
        setupTopCards();
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("May take the sole permanent without a reorder prompt")
    void choosesOnlyCard() {
        Forest land = new Forest();
        setupTopCards(land);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("May decline even when the library contains only one permanent")
    void declinesOnlyCard() {
        Forest land = new Forest();
        setupTopCards(land);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("With two cards, may take a permanent and bottom the other without reordering")
    void twoCardLibrary() {
        SamutsSprint instant = new SamutsSprint();
        Forest land = new Forest();
        setupTopCards(instant, land);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new BondOfFlourishing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setupTopCards(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
