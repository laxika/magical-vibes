package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnexpectedConversion.class, Shock.class, LightningBolt.class, Forest.class, Island.class})
class UnexpectedConversionTest extends BaseCardTest {

    @Test
    void exilesChosenCopiesAndSeeksForHandCopies() {
        Shock chosenCard = new Shock();
        Shock handCopy = new Shock();
        Shock libraryCopy = new Shock();
        LightningBolt soughtCard = new LightningBolt();

        harness.setHand(player1, List.of(new UnexpectedConversion(), chosenCard, handCopy));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), libraryCopy, soughtCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.RevealedHandChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(handChoice).isNotNull();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(chosenCard));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(chosenCard, handCopy, libraryCopy);
        assertThat(gd.playerHands.get(player1.getId())).contains(soughtCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(soughtCard);
    }

    @Test
    void mayDeclineTheInitialExile() {
        Shock cardToKeep = new Shock();
        LightningBolt remainingCard = new LightningBolt();
        harness.setHand(player1, List.of(new UnexpectedConversion(), cardToKeep));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), remainingCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(cardToKeep);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    void seeksForTheInitiallyExiledCardWhenThereAreNoOtherCopies() {
        Shock chosenCard = new Shock();
        LightningBolt soughtCard = new LightningBolt();
        Forest drawnForest = new Forest();
        Island drawnIsland = new Island();
        harness.setHand(player1, List.of(new UnexpectedConversion(), chosenCard));
        harness.setLibrary(player1, List.of(drawnForest, drawnIsland, soughtCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(chosenCard));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(drawnForest, drawnIsland, soughtCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void seeksForTheInitialCardEvenWhenNoAdditionalCopiesAreSelected() {
        Shock chosenCard = new Shock();
        Shock handCopy = new Shock();
        UnexpectedConversion soughtCard = new UnexpectedConversion();
        Forest drawnForest = new Forest();
        Island drawnIsland = new Island();
        harness.setHand(player1, List.of(new UnexpectedConversion(), chosenCard, handCopy));
        harness.setLibrary(player1, List.of(drawnForest, drawnIsland, soughtCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(chosenCard));
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(handCopy, drawnForest, drawnIsland, soughtCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void countsBothInitialAndAdditionalHandExilesButNotLibraryExiles() {
        Shock chosenCard = new Shock();
        Shock handCopy = new Shock();
        Shock libraryCopy = new Shock();
        LightningBolt firstSoughtCard = new LightningBolt();
        LightningBolt secondSoughtCard = new LightningBolt();
        LightningBolt thirdEligibleCard = new LightningBolt();
        Forest drawnForest = new Forest();
        Island drawnIsland = new Island();
        Forest remainingLand = new Forest();
        harness.setHand(player1, List.of(new UnexpectedConversion(), chosenCard, handCopy));
        harness.setLibrary(player1, List.of(drawnForest, drawnIsland, libraryCopy,
                firstSoughtCard, secondSoughtCard, thirdEligibleCard, remainingLand));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(chosenCard));
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(chosenCard, handCopy, libraryCopy);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(drawnForest, drawnIsland);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof LightningBolt).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(remainingLand);
    }

    @Test
    void canExileASorceryDrawnByTheSpell() {
        UnexpectedConversion drawnSorcery = new UnexpectedConversion();
        LightningBolt soughtCard = new LightningBolt();
        Forest drawnForest = new Forest();
        harness.setHand(player1, List.of(new UnexpectedConversion()));
        harness.setLibrary(player1, List.of(drawnSorcery, drawnForest, soughtCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(drawnSorcery));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(drawnSorcery);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnForest, soughtCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void finishesAfterDrawingWhenTheHandContainsNoInstantOrSorcery() {
        Forest drawnForest = new Forest();
        Island drawnIsland = new Island();
        Forest remainingLand = new Forest();
        harness.setHand(player1, List.of(new UnexpectedConversion()));
        harness.setLibrary(player1, List.of(drawnForest, drawnIsland, remainingLand));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnForest, drawnIsland);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLand);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
