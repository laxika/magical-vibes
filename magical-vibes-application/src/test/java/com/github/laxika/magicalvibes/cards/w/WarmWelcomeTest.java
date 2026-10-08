package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReadyToRumble;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarmWelcome.class, GrizzlyBears.class, Shock.class, CivicGardener.class, ReadyToRumble.class})
class WarmWelcomeTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing a creature puts it into hand and creates a Citizen")
    void revealsCreatureAndCreatesCitizen() {
        Card creature = new GrizzlyBears();
        List<Card> topCards = List.of(creature, new Shock(), new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, topCards);

        resolveWarmWelcome();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                topCards.get(1), topCards.get(2), topCards.get(3), topCards.get(4));
        assertCitizenToken();
    }

    @Test
    @DisplayName("Declining the creature keeps the hand empty and still creates a Citizen")
    void decliningCreatureStillCreatesCitizen() {
        List<Card> topCards = List.of(new GrizzlyBears(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, topCards);

        resolveWarmWelcome();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertCitizenToken();
    }

    @Test
    @DisplayName("With no creature among the top five, all cards go to the bottom and a Citizen is created")
    void noCreatureStillCreatesCitizen() {
        List<Card> topCards = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.setLibrary(player1, topCards);

        resolveWarmWelcome();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertCitizenToken();
    }

    @Test
    @DisplayName("An empty library still produces a Citizen without a choice")
    void emptyLibraryStillCreatesCitizen() {
        harness.setLibrary(player1, List.of());

        resolveWarmWelcome();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertCitizenToken();
    }

    @Test
    @DisplayName("The only card in a short library can be revealed and taken")
    void takesOnlyCreatureFromShortLibrary() {
        Card creature = new CivicGardener();
        harness.setLibrary(player1, List.of(creature));

        resolveWarmWelcome();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertCitizenToken();
    }

    @Test
    @DisplayName("Taking the only creature in a short library is still optional")
    void declinesOnlyCreatureFromShortLibrary() {
        Card creature = new CivicGardener();
        harness.setLibrary(player1, List.of(creature));

        resolveWarmWelcome();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertCitizenToken();
    }

    @Test
    @DisplayName("Only one creature from the top five may be taken, and the untouched library stays above the rest")
    void takesOneCreatureAndPreservesUntouchedLibraryOrder() {
        Card firstCreature = new CivicGardener();
        Card chosenCreature = new CivicGardener();
        Card noncreature = new ReadyToRumble();
        List<Card> topCards = List.of(firstCreature, noncreature, chosenCreature,
                new ReadyToRumble(), new ReadyToRumble());
        Card sixthCard = new CivicGardener();
        Card seventhCard = new ReadyToRumble();
        harness.setLibrary(player1, List.of(topCards.get(0), topCards.get(1), topCards.get(2),
                topCards.get(3), topCards.get(4), sixthCard, seventhCard));

        resolveWarmWelcome();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(noncreature.getId()))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(sixthCard.getId()))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstCreature.getId(), chosenCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(chosenCreature.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCreature);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.subList(0, 2)).containsExactly(sixthCard, seventhCard);
        assertThat(library.subList(2, 6)).containsExactlyInAnyOrder(
                firstCreature, noncreature, topCards.get(3), topCards.get(4));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertCitizenToken();
    }

    @Test
    @DisplayName("A creature below the top five is not found and the looked-at cards go beneath it")
    void noCreatureInTopFivePreservesUntouchedLibrary() {
        List<Card> topCards = List.of(new ReadyToRumble(), new ReadyToRumble(),
                new ReadyToRumble(), new ReadyToRumble(), new ReadyToRumble());
        Card sixthCard = new CivicGardener();
        harness.setLibrary(player1, List.of(topCards.get(0), topCards.get(1), topCards.get(2),
                topCards.get(3), topCards.get(4), sixthCard));

        resolveWarmWelcome();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.getFirst()).isSameAs(sixthCard);
        assertThat(library.subList(1, 6)).containsExactlyInAnyOrderElementsOf(topCards);
        assertCitizenToken();
    }

    private void resolveWarmWelcome() {
        harness.setHand(player1, List.of(new WarmWelcome()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }

    private void assertCitizenToken() {
        List<Permanent> citizens = findPermanents(player1, "Citizen").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(citizens).hasSize(1);
        Permanent citizen = citizens.getFirst();
        assertThat(citizen.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(citizen.getCard().getPower()).isEqualTo(1);
        assertThat(citizen.getCard().getToughness()).isEqualTo(1);
        assertThat(citizen.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(citizen.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
    }
}
