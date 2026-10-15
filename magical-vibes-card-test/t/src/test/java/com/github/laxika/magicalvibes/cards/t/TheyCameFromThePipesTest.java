package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheyCameFromThePipes.class, Forest.class, GrizzlyBears.class})
class TheyCameFromThePipesTest extends BaseCardTest {

    @Test
    void entersAndManifestsDreadTwice() {
        Card firstManifested = new GrizzlyBears();
        Card firstGraveyard = new Forest();
        Card secondManifested = new GrizzlyBears();
        Card secondGraveyard = new Forest();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstManifested, firstGraveyard, secondManifested, secondGraveyard,
                firstDraw, secondDraw));
        harness.castFromHand(player1, new TheyCameFromThePipes(), "{4}{U}");
        resolveAllTriggers();
        chooseManifested(firstManifested);
        resolveAllTriggers();
        chooseManifested(secondManifested);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstGraveyard, secondGraveyard);
    }

    @Test
    void drawsWheneverAFaceDownCreatureEntersUnderYourControl() {
        Card manifested = new GrizzlyBears();
        Card graveyard = new Forest();
        Card secondManifested = new GrizzlyBears();
        Card secondGraveyard = new Forest();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setLibrary(player1, List.of(manifested, graveyard, secondManifested, secondGraveyard,
                firstDraw, secondDraw));
        harness.castFromHand(player1, new TheyCameFromThePipes(), "{4}{U}");
        resolveAllTriggers();
        chooseManifested(manifested);
        resolveAllTriggers();
        chooseManifested(secondManifested);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void manifestingLandsDrawsOnlyAfterBothManifestActionsFinish() {
        Card firstManifested = new Forest();
        Card firstGraveyard = new Forest();
        Card secondManifested = new Forest();
        Card secondGraveyard = new Forest();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstManifested, firstGraveyard, secondManifested,
                secondGraveyard, firstDraw, secondDraw));

        harness.castFromHand(player1, new TheyCameFromThePipes(), "{4}{U}");
        resolveAllTriggers();
        chooseManifested(firstManifested);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        chooseManifested(secondManifested);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .extracting(Permanent::getOriginalCard)
                .containsExactlyInAnyOrder(firstManifested, secondManifested);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstGraveyard, secondGraveyard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void faceUpCreaturesEnteringDoNotDrawCards() {
        harness.addToBattlefield(player1, new TheyCameFromThePipes());
        Card draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void opponentsFaceDownCreaturesDoNotTriggerYourEnchantment() {
        harness.addToBattlefield(player1, new TheyCameFromThePipes());
        Card yourDraw = new Forest();
        harness.setLibrary(player1, List.of(yourDraw));
        harness.setHand(player1, List.of());
        Card firstManifested = new Forest();
        Card secondManifested = new Forest();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setLibrary(player2, List.of(firstManifested, new Forest(), secondManifested,
                new Forest(), firstDraw, secondDraw));

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new TheyCameFromThePipes(), "{4}{U}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player2, List.of(firstManifested.getId()));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player2, List.of(secondManifested.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(Permanent::isManifested).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(yourDraw);
    }

    @Test
    void turningAManifestedCreatureFaceUpDoesNotDrawAgain() {
        Card firstManifested = new GrizzlyBears();
        Card secondManifested = new GrizzlyBears();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(firstManifested, new Forest(), secondManifested,
                new Forest(), firstDraw, secondDraw, remaining));
        harness.castFromHand(player1, new TheyCameFromThePipes(), "{4}{U}");
        resolveAllTriggers();
        chooseManifested(firstManifested);
        resolveAllTriggers();
        chooseManifested(secondManifested);
        resolveAllTriggers();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(
                gd.playerBattlefields.get(player1.getId()).stream()
                        .filter(p -> p.getOriginalCard() == firstManifested)
                        .findFirst().orElseThrow());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, index);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).get(index).isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    private void chooseManifested(Card card) {
        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }
}
