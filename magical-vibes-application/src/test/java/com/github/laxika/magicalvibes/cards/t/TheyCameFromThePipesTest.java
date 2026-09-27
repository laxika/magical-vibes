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
        harness.setHand(player1, List.of(new TheyCameFromThePipes()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
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
        harness.setHand(player1, List.of(new TheyCameFromThePipes()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        chooseManifested(manifested);
        resolveAllTriggers();
        chooseManifested(secondManifested);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    private void chooseManifested(Card card) {
        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }
}
