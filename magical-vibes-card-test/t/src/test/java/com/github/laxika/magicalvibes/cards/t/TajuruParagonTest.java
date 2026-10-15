package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AcquisitionsExpert;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KazanduNectarpot;
import com.github.laxika.magicalvibes.cards.k.KorCelebrant;
import com.github.laxika.magicalvibes.cards.s.SquadCommander;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruParagon.class, TazeemRoilmage.class, KazanduNectarpot.class,
        KorCelebrant.class, AcquisitionsExpert.class, SquadCommander.class, IntoTheRoil.class})
class TajuruParagonTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotLookAtLibrary() {
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, List.of(new TazeemRoilmage()));
        addMana(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void kickedMayPutOneMatchingCreatureTypeCardIntoHand() {
        Card matchingCard = new TazeemRoilmage();
        Card nonmatchingCard = new KazanduNectarpot();
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, List.of(matchingCard, nonmatchingCard));
        addMana(4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(matchingCard.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(matchingCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatchingCard);
    }

    @Test
    void kickedWithNoMatchingCardBottomsRevealedCards() {
        Card first = new KazanduNectarpot();
        Card second = new KazanduNectarpot();
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, List.of(first, second));
        addMana(4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void kickedRevealsAllCardsIncludingUnchosenNonmatchingCards() {
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, List.of(new TazeemRoilmage(), new KazanduNectarpot()));
        addMana(4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals", "Tazeem Roilmage", "Kazandu Nectarpot"));
    }

    @Test
    void kickedCanDeclineMatchingCardAndBottomsOnlyTopSix() {
        Card matching = new TazeemRoilmage();
        List<Card> topSix = List.of(matching, new KazanduNectarpot(), new KazanduNectarpot(),
                new KazanduNectarpot(), new KazanduNectarpot(), new KazanduNectarpot());
        Card seventh = new KorCelebrant();
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, Stream.concat(topSix.stream(), Stream.of(seventh)).toList());
        addMana(4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(matching.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7)).containsExactlyInAnyOrderElementsOf(topSix);
    }

    @Test
    void kickedAcceptsEachGrantedCreatureTypeAndItsOriginalElfType() {
        List<Card> matching = List.of(new KorCelebrant(), new AcquisitionsExpert(),
                new SquadCommander(), new TazeemRoilmage(), new TajuruParagon());
        Card nonmatching = new KazanduNectarpot();
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, Stream.concat(matching.stream(), Stream.of(nonmatching)).toList());
        addMana(4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(
                matching.stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, List.of(matching.getFirst().getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matching.getFirst());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).contains(nonmatching);
    }

    @Test
    void kickedWithEmptyLibraryDoesNotAskForChoice() {
        harness.setHand(player1, List.of(new TajuruParagon()));
        harness.setLibrary(player1, List.of());
        addMana(4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void kickedUsesLastKnownCreatureTypesAfterLeavingBattlefield() {
        Card matching = new TazeemRoilmage();
        harness.setHand(player1, List.of(new TajuruParagon(), new IntoTheRoil()));
        harness.setLibrary(player1, List.of(matching));
        addMana(4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Tajuru Paragon"));
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(matching.getId());
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(matching);
        harness.assertInHand(player1, "Tajuru Paragon");
        harness.assertNotOnBattlefield(player1, "Tajuru Paragon");
    }

    private void addMana(int colorless) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
