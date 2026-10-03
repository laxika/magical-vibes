package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjanisInfluence.class, GrizzlyBears.class, Pacifism.class, Shock.class, Island.class, Swamp.class})
class AjanisInfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on the target creature and offers a white card")
    void putsCountersAndOffersWhiteCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Pacifism pacifism = new Pacifism();
        setupTopCards(List.of(new Shock(), pacifism, new Island(), new Swamp(), new GrizzlyBears()));

        cast(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactly(pacifism.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Puts the chosen white card into hand and the rest on the library bottom")
    void choosesWhiteCardAndRandomizesTheRest() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Pacifism pacifism = new Pacifism();
        List<Card> topCards = List.of(pacifism, new Shock(), new Island(), new Swamp(), new GrizzlyBears());
        setupTopCards(topCards);

        cast(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(pacifism.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(pacifism);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards.subList(1, 5));
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("May decline the white card and put all five cards on the library bottom")
    void mayDeclineWhiteCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Card> topCards = List.of(new Pacifism(), new Shock(), new Island(), new Swamp(), new GrizzlyBears());
        setupTopCards(topCards);

        cast(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsUnchosenCardsBelowCardsOutsideTheTopFive() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Pacifism whiteCard = new Pacifism();
        List<Card> remaining = List.of(new Shock(), new Island(), new Swamp(), new GrizzlyBears());
        Island sixthCard = new Island();
        Swamp seventhCard = new Swamp();
        setupTopCards(List.of(whiteCard, remaining.get(0), remaining.get(1), remaining.get(2),
                remaining.get(3), sixthCard, seventhCard));

        cast(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(whiteCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(whiteCard);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.subList(0, 2)).containsExactly(sixthCard, seventhCard);
        assertThat(library.subList(2, 6)).containsExactlyInAnyOrderElementsOf(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithFewerThanFiveCardsAndAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Pacifism whiteCard = new Pacifism();
        Shock otherCard = new Shock();
        setupTopCards(List.of(whiteCard, otherCard));

        cast(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(whiteCard.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(whiteCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsAllLookedAtCardsOnTheBottomWhenNoneAreWhite() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Card> topCards = List.of(new Shock(), new Island(), new Swamp(), new GrizzlyBears(), new Shock());
        Pacifism sixthCard = new Pacifism();
        setupTopCards(List.of(topCards.get(0), topCards.get(1), topCards.get(2), topCards.get(3),
                topCards.get(4), sixthCard));

        cast(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.getFirst()).isSameAs(sixthCard);
        assertThat(library.subList(1, 6)).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void stillPlacesCountersWithAnEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupTopCards(List.of());

        cast(target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotLookAtTheLibraryWhenItsOnlyTargetDiesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        List<Card> library = List.of(new Pacifism(), new Shock(), new Island(), new Swamp(), new GrizzlyBears());
        setupTopCards(library);
        harness.setHand(player1, List.of(new AjanisInfluence()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ajani's Influence");
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, List.of(new AjanisInfluence()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
