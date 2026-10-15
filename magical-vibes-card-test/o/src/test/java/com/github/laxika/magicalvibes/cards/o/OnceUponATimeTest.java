package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnceUponATime.class, GrizzlyBears.class, Forest.class, Shock.class})
class OnceUponATimeTest extends BaseCardTest {

    @Test
    void firstSpellCanBeCastWithoutPayingMana() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card filler1 = new Shock();
        Card filler2 = new Shock();
        Card filler3 = new Shock();
        harness.setLibrary(player1, List.of(creature, filler1, land, filler2, filler3));
        harness.setHand(player1, List.of(new OnceUponATime()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, filler1, filler2, filler3);
    }

    @Test
    void freeAlternateCostIsUnavailableAfterCastingAnotherSpell() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.setHand(player1, List.of(new OnceUponATime()));
        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayNormalManaCostAfterCastingAnotherSpellAndChooseLand() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Card land = new Forest();
        Card filler = new Shock();
        harness.setLibrary(player1, List.of(land, filler));
        harness.setHand(player1, List.of(new OnceUponATime()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
        harness.assertInGraveyard(player1, "Once Upon a Time");
    }

    @Test
    void mayDeclineEvenWhenEligibleCardsArePresent() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card filler1 = new Shock();
        Card filler2 = new Shock();
        Card filler3 = new Shock();
        Card untouched = new Forest();
        List<Card> lookedAt = List.of(creature, land, filler1, filler2, filler3);
        harness.setLibrary(player1, List.of(creature, land, filler1, filler2, filler3, untouched));
        harness.setHand(player1, List.of(new OnceUponATime()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Once Upon a Time");
    }

    @Test
    void noEligibleCardsGoToBottomWithoutAChoice() {
        Card filler1 = new Shock();
        Card filler2 = new Shock();
        Card filler3 = new Shock();
        Card filler4 = new Shock();
        Card filler5 = new Shock();
        Card untouched = new GrizzlyBears();
        List<Card> lookedAt = List.of(filler1, filler2, filler3, filler4, filler5);
        harness.setLibrary(player1, List.of(filler1, filler2, filler3, filler4, filler5, untouched));
        harness.setHand(player1, List.of(new OnceUponATime()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Once Upon a Time");
    }

    @Test
    void selectedCardLeavesRestBelowUntouchedLibrary() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card filler1 = new Shock();
        Card filler2 = new Shock();
        Card filler3 = new Shock();
        Card untouched1 = new Forest();
        Card untouched2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, land, filler1, filler2, filler3, untouched1, untouched2));
        harness.setHand(player1, List.of(new OnceUponATime()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(filler1.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(untouched1.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(untouched1, untouched2);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrder(land, filler1, filler2, filler3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryResolvesWithoutDrawingOrRequestingInput() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new OnceUponATime()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Once Upon a Time");
    }

    @Test
    void playingLandDoesNotPreventFreeFirstSpell() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new Forest(), new OnceUponATime()));
        harness.playLand(player1, 0);

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsSpellDoesNotPreventFreeFirstSpell() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new OnceUponATime()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
    }

    @Test
    void firstCopyAlreadyOnStackPreventsCastingSecondCopyForFree() {
        harness.setHand(player1, List.of(new OnceUponATime(), new OnceUponATime()));
        harness.setLibrary(player1, List.of(new Shock()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Once Upon a Time");
    }
}
