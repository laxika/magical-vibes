package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WarmWelcome.class, GrizzlyBears.class, Shock.class})
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

    private void resolveWarmWelcome() {
        harness.setHand(player1, List.of(new WarmWelcome()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void assertCitizenToken() {
        List<Permanent> citizens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Citizen"))
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
