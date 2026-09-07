package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DonatelloMutantMechanic;
import com.github.laxika.magicalvibes.cards.m.MightyMutanimals;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({Cowabunga.class, DonatelloMutantMechanic.class, MightyMutanimals.class, Plains.class, Shock.class})
class CowabungaTest extends BaseCardTest {

    @Test
    @DisplayName("Offers Mutant and land cards among the top four")
    void offersMatchingCards() {
        Card mutant = new DonatelloMutantMechanic();
        Card otherMutant = new MightyMutanimals();
        Card land = new Plains();
        Card spell = new Shock();
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
        Card spell = new Shock();
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
        Card spell = new Shock();
        castWithTopCards(mutant, otherMutant, land, spell);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardsChosen(List.of()));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mutant, otherMutant, land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(mutant, otherMutant, land, spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no matching cards, all four looked-at cards go to the library bottom")
    void noMatchingCards() {
        Card first = new Shock();
        Card second = new Shock();
        Card third = new Shock();
        Card fourth = new Shock();
        castWithTopCards(first, second, third, fourth);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);
    }

    private void castWithTopCards(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
        harness.setHand(player1, List.of(new Cowabunga()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
