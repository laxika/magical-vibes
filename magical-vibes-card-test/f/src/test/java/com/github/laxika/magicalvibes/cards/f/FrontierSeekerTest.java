package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontierSeeker.class, GrizzlyBears.class, Plains.class, Shock.class, TrainedArynx.class})
class FrontierSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Offers Mount creature cards and Plains cards from the top five")
    void offersMountCreaturesAndPlains() {
        GrizzlyBears mount = mountCreature();
        Plains plains = new Plains();
        setupTopFive(List.of(mount, new Shock(), plains, new GrizzlyBears(), new Shock()));
        castFrontierSeeker();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(mount.getId(), plains.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a chosen Mount or Plains card into hand and randomizes the rest to the bottom")
    void choosesMatchingCardAndBottomsTheRest() {
        GrizzlyBears mount = mountCreature();
        Plains plains = new Plains();
        Shock shockOne = new Shock();
        Shock shockTwo = new Shock();
        GrizzlyBears regularCreature = new GrizzlyBears();
        setupTopFive(List.of(shockOne, mount, plains, shockTwo, regularCreature));
        castFrontierSeeker();

        harness.handleMultipleCardsChosen(player1, List.of(mount.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(mount.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shockOne.getId(), plains.getId(), shockTwo.getId(),
                        regularCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May decline the matching card and bottom all five cards")
    void mayDecline() {
        GrizzlyBears mount = mountCreature();
        Plains plains = new Plains();
        setupTopFive(List.of(mount, plains, new Shock(), new GrizzlyBears(), new Shock()));
        castFrontierSeeker();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(mount.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(plains.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A real Mount can be chosen while the untouched library stays above the bottomed cards")
    void choosesRealMountAndPreservesUntouchedLibrary() {
        TrainedArynx mount = new TrainedArynx();
        Plains plains = new Plains();
        FrontierSeeker first = new FrontierSeeker();
        FrontierSeeker second = new FrontierSeeker();
        FrontierSeeker third = new FrontierSeeker();
        Plains untouchedFirst = new Plains();
        TrainedArynx untouchedSecond = new TrainedArynx();
        harness.setLibrary(player1, List.of(mount, plains, first, second, third,
                untouchedFirst, untouchedSecond));
        castFrontierSeeker();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(mount.getId(), plains.getId());
        harness.handleMultipleCardsChosen(player1, List.of(mount.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mount);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(untouchedFirst, untouchedSecond);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrder(plains, first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May choose Plains when the library contains fewer than five cards")
    void choosesPlainsFromShortLibrary() {
        Plains plains = new Plains();
        FrontierSeeker nonmatching = new FrontierSeeker();
        harness.setLibrary(player1, List.of(nonmatching, plains));
        castFrontierSeeker();

        harness.handleMultipleCardsChosen(player1, List.of(plains.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May decline even when the only card in the library is eligible")
    void mayDeclineOnlyEligibleCard() {
        TrainedArynx mount = new TrainedArynx();
        harness.setLibrary(player1, List.of(mount));
        castFrontierSeeker();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mount);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching cards are bottomed without offering a choice or looking at the sixth card")
    void noMatchesAmongTopFive() {
        List<Card> topFive = List.of(new FrontierSeeker(), new FrontierSeeker(),
                new FrontierSeeker(), new FrontierSeeker(), new FrontierSeeker());
        Plains sixth = new Plains();
        java.util.ArrayList<Card> library = new java.util.ArrayList<>(topFive);
        library.add(sixth);
        harness.setLibrary(player1, library);
        castFrontierSeeker();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a draw")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        castFrontierSeeker();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castFrontierSeeker() {
        harness.setHand(player1, List.of(new FrontierSeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void setupTopFive(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private GrizzlyBears mountCreature() {
        GrizzlyBears mount = new GrizzlyBears();
        mount.setSubtypes(List.of(CardSubtype.MOUNT));
        return mount;
    }
}
