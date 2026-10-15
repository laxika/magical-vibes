package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FellGravship;
import com.github.laxika.magicalvibes.cards.f.FocusFire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PulsarSquadronAce.class, FellGravship.class, FocusFire.class})
class PulsarSquadronAceTest extends BaseCardTest {

    @Test
    void choosingSpacecraftPutsItIntoHandWithoutAddingCounter() {
        Card spacecraft = new FellGravship();
        List<Card> topCards = List.of(new FocusFire(), spacecraft,
                new FocusFire(), new FocusFire(), new FocusFire());
        harness.setLibrary(player1, topCards);

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
        Card spacecraft = new FellGravship();
        List<Card> topCards = List.of(spacecraft, new FocusFire(), new FocusFire(),
                new FocusFire(), new FocusFire());
        harness.setLibrary(player1, topCards);

        Permanent ace = castAndResolveEtb();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spacecraft);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(ace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void noSpacecraftPutsCounterOnAceWithoutChoice() {
        List<Card> topCards = List.of(new FocusFire(), new FocusFire(),
                new FocusFire(), new FocusFire(), new FocusFire());
        harness.setLibrary(player1, topCards);

        Permanent ace = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(ace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void emptyLibraryStillPutsCounterOnAce() {
        harness.setLibrary(player1, List.of());

        Permanent ace = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(ace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void shortLibraryAllowsChoosingOnlyOneOfMultipleSpacecraft() {
        Card chosen = new FellGravship();
        Card other = new FellGravship();
        Card spell = new FocusFire();
        harness.setLibrary(player1, List.of(chosen, spell, other));

        Permanent ace = castAndResolveEtb();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), other.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(spell, other);
        assertThat(ace.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void spacecraftBelowTopFiveIsNotEligibleAndStaysAboveBottomedCards() {
        List<Card> topCards = List.of(new FocusFire(), new FocusFire(), new FocusFire(),
                new FocusFire(), new FocusFire());
        Card sixth = new FellGravship();
        Card seventh = new FocusFire();
        harness.setLibrary(player1, List.of(topCards.get(0), topCards.get(1), topCards.get(2),
                topCards.get(3), topCards.get(4), sixth, seventh));

        Permanent ace = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 7))
                .containsExactlyInAnyOrderElementsOf(topCards);
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

}
