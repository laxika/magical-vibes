package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PulsarSquadronAce.class)
class PulsarSquadronAceTest extends BaseCardTest {

    @Test
    void choosingSpacecraftPutsItIntoHandWithoutAddingCounter() {
        Card spacecraft = spacecraft("Test Spacecraft");
        List<Card> topCards = List.of(nonSpacecraft("Spell 1"), spacecraft,
                nonSpacecraft("Spell 2"), nonSpacecraft("Spell 3"), nonSpacecraft("Spell 4"));
        setLibrary(topCards);

        Permanent ace = castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(spacecraft.getId());

        harness.handleMultipleCardsChosen(player1, List.of(spacecraft.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(spacecraft);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                topCards.get(0), topCards.get(2), topCards.get(3), topCards.get(4));
        assertThat(ace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void decliningSpacecraftPutsCounterOnAce() {
        Card spacecraft = spacecraft("Test Spacecraft");
        List<Card> topCards = List.of(spacecraft, nonSpacecraft("Spell 1"), nonSpacecraft("Spell 2"),
                nonSpacecraft("Spell 3"), nonSpacecraft("Spell 4"));
        setLibrary(topCards);

        Permanent ace = castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spacecraft);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(ace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void noSpacecraftPutsCounterOnAceWithoutChoice() {
        List<Card> topCards = List.of(nonSpacecraft("Spell 1"), nonSpacecraft("Spell 2"),
                nonSpacecraft("Spell 3"), nonSpacecraft("Spell 4"), nonSpacecraft("Spell 5"));
        setLibrary(topCards);

        Permanent ace = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(ace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castAndResolveEtb() {
        harness.setHand(player1, List.of(new PulsarSquadronAce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Pulsar Squadron Ace");
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

    private Card spacecraft(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.SPACECRAFT));
        return card;
    }

    private Card nonSpacecraft(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        return card;
    }
}
