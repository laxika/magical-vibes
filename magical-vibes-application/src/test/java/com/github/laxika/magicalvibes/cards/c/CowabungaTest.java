package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DonatelloMutantMechanic;
import com.github.laxika.magicalvibes.cards.f.FootNinjas;
import com.github.laxika.magicalvibes.cards.m.MightyMutanimals;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cowabunga.class, DonatelloMutantMechanic.class, FootNinjas.class, MightyMutanimals.class, Plains.class})
class CowabungaTest extends BaseCardTest {

    @Test
    @DisplayName("Offers Mutant and land cards among the top four")
    void offersMatchingCards() {
        Card mutant = new DonatelloMutantMechanic();
        Card otherMutant = new MightyMutanimals();
        Card land = new Plains();
        Card spell = new Cowabunga();
        castWithTopCards(mutant, otherMutant, land, spell);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                mutant.getId(), otherMutant.getId(), land.getId());
    }

    @Test
    @DisplayName("Chosen matching card goes to hand and the rest go to the library bottom")
    void choosesMatchingCard() {
        Card chosen = new DonatelloMutantMechanic();
        Card otherMutant = new MightyMutanimals();
        Card land = new Plains();
        Card spell = new Cowabunga();
        castWithTopCards(chosen, otherMutant, land, spell);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(otherMutant, land, spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May decline and put all four looked-at cards on the library bottom")
    void mayDecline() {
        Card mutant = new DonatelloMutantMechanic();
        Card otherMutant = new MightyMutanimals();
        Card land = new Plains();
        Card spell = new Cowabunga();
        castWithTopCards(mutant, otherMutant, land, spell);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mutant, otherMutant, land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(mutant, otherMutant, land, spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no matching cards, all four looked-at cards go to the library bottom")
    void noMatchingCards() {
        Card first = new Cowabunga();
        Card second = new Cowabunga();
        Card third = new Cowabunga();
        Card fourth = new Cowabunga();
        castWithTopCards(first, second, third, fourth);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);
    }

    @Test
    @DisplayName("The caster can look at ineligible cards as well as eligible cards")
    void showsAllLookedAtCards() {
        Card land = new Plains();
        Card spell = new Cowabunga();
        harness.clearMessages();
        castWithTopCards(land, spell);

        assertThat(harness.getConn1().getMessagesContaining(spell.getId().toString())).isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining(spell.getId().toString())).isEmpty();
    }

    @Test
    @DisplayName("A Human Ninja can be chosen without being a Mutant or Turtle")
    void choosesNinja() {
        Card ninja = new FootNinjas();
        Card spell = new Cowabunga();
        castWithTopCards(ninja, spell);

        harness.handleMultipleCardsChosen(player1, List.of(ninja.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ninja);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A land can be taken from a library with fewer than four cards")
    void choosesLandFromShortLibrary() {
        Card land = new Plains();
        castWithTopCards(land);

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The only eligible card may still be declined")
    void declinesOnlyCard() {
        Card land = new Plains();
        castWithTopCards(land);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unseen cards stay above the random bottom pile and cannot be chosen")
    void looksOnlyAtTopFour() {
        Card chosen = new MightyMutanimals();
        Card first = new Cowabunga();
        Card second = new Cowabunga();
        Card third = new Cowabunga();
        Card unseenLand = new Plains();
        Card unseenNinja = new FootNinjas();
        castWithTopCards(chosen, first, second, third, unseenLand, unseenNinja);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(unseenLand.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(unseenLand, unseenNinja);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 5))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("At most one eligible card can be taken")
    void rejectsMultipleSelections() {
        Card mutant = new MightyMutanimals();
        Card land = new Plains();
        castWithTopCards(mutant, land);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(mutant.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(mutant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mutant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibrary() {
        castWithTopCards();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cowabunga!");
    }

    private void castWithTopCards(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
        harness.setHand(player1, List.of(new Cowabunga()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
