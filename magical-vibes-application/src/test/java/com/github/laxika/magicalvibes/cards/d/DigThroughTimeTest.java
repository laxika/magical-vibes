package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.cards.s.SwiftwaterCliffs;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DigThroughTime.class, AlpineGrizzly.class, WetlandSambar.class, SwiftwaterCliffs.class})
class DigThroughTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost, then two cards go to hand and the rest bottom in order")
    void delvesAndLooksAtTopSeven() {
        Card top1 = new AlpineGrizzly();
        Card top2 = new WetlandSambar();
        Card top3 = new SwiftwaterCliffs();
        Card top4 = new AlpineGrizzly();
        Card top5 = new WetlandSambar();
        Card top6 = new SwiftwaterCliffs();
        Card top7 = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4, top5, top6, top7));

        List<Card> graveyard = List.of(
                new SwiftwaterCliffs(), new AlpineGrizzly(), new WetlandSambar(),
                new SwiftwaterCliffs(), new AlpineGrizzly(), new WetlandSambar());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0, 1, 2, 3, 4, 5));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId(), top2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top1, top2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).containsExactlyInAnyOrder(top3, top4, top5, top6, top7);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(reorder.indexOf(top3), reorder.indexOf(top4), reorder.indexOf(top5),
                        reorder.indexOf(top6), reorder.indexOf(top7))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top3, top4, top5, top6, top7);
        harness.assertInGraveyard(player1, "Dig Through Time");
    }

    @Test
    @DisplayName("Pays only for the number of cards actually chosen for delve")
    void partialDelveReduction() {
        harness.setLibrary(player1, List.of(
                new AlpineGrizzly(), new WetlandSambar(), new SwiftwaterCliffs(),
                new AlpineGrizzly(), new WetlandSambar(), new SwiftwaterCliffs(), new AlpineGrizzly()));
        List<Card> graveyard = List.of(
                new SwiftwaterCliffs(), new AlpineGrizzly(), new WetlandSambar(),
                new SwiftwaterCliffs(), new AlpineGrizzly(), new WetlandSambar());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyard.get(2), graveyard.get(3),
                graveyard.get(4), graveyard.get(5));
    }

    @Test
    void leavesUnseenCardsAboveTheReorderedRemainder() {
        List<Card> lookedAt = List.of(new AlpineGrizzly(), new WetlandSambar(), new SwiftwaterCliffs(),
                new AlpineGrizzly(), new WetlandSambar(), new SwiftwaterCliffs(), new AlpineGrizzly());
        Card unseen = new WetlandSambar();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), lookedAt.get(5), lookedAt.get(6), unseen));
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(lookedAt.get(0).getId(), unseen.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(lookedAt.get(4).getId(), lookedAt.get(6).getId()));
        List<Card> remainder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        List<Card> bottomOrder = List.of(lookedAt.get(5), lookedAt.get(3), lookedAt.get(2),
                lookedAt.get(1), lookedAt.get(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                bottomOrder.stream().map(remainder::indexOf).toList()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(lookedAt.get(4), lookedAt.get(6));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, lookedAt.get(5),
                lookedAt.get(3), lookedAt.get(2), lookedAt.get(1), lookedAt.get(0));
        harness.assertInGraveyard(player1, "Dig Through Time");
    }

    @Test
    void takesOnlyTwoOfAShortLibrary() {
        Card first = new AlpineGrizzly();
        Card second = new WetlandSambar();
        Card third = new SwiftwaterCliffs();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        harness.assertInGraveyard(player1, "Dig Through Time");
    }

    @Test
    void putsTheOnlyAvailableCardIntoHandWithoutDrawing() {
        Card onlyCard = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Dig Through Time");
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Dig Through Time");
    }

    @Test
    void delveCannotPayForBlueMana() {
        List<Card> graveyard = List.of(new AlpineGrizzly(), new WetlandSambar(), new SwiftwaterCliffs(),
                new AlpineGrizzly(), new WetlandSambar(), new SwiftwaterCliffs());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, null, List.of(0, 1, 2, 3, 4, 5)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Dig Through Time");
    }
}
