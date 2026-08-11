package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DigThroughTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost, then two cards go to hand and the rest bottom in order")
    void delvesAndLooksAtTopSeven() {
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        Card top3 = new Shock();
        Card top4 = new GrizzlyBears();
        Card top5 = new LlanowarElves();
        Card top6 = new Shock();
        Card top7 = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(top1, top2, top3, top4, top5, top6, top7));

        List<Card> graveyard = List.of(
                new Shock(), new GrizzlyBears(), new LlanowarElves(),
                new Shock(), new GrizzlyBears(), new LlanowarElves());
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
                new GrizzlyBears(), new LlanowarElves(), new Shock(),
                new GrizzlyBears(), new LlanowarElves(), new Shock(), new GrizzlyBears()));
        List<Card> graveyard = List.of(
                new Shock(), new GrizzlyBears(), new LlanowarElves(),
                new Shock(), new GrizzlyBears(), new LlanowarElves());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new DigThroughTime()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyard.get(2), graveyard.get(3),
                graveyard.get(4), graveyard.get(5));
    }
}
