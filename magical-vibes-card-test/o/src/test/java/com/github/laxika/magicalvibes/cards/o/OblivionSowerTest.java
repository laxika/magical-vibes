package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OblivionSower.class, Forest.class, Mountain.class, GrizzlyBears.class})
class OblivionSowerTest extends BaseCardTest {

    @Test
    void offersAllFaceUpLandsOwnedByTheTargetPlayerFromExile() {
        Forest previouslyExiled = new Forest();
        Forest faceDownLand = new Forest();
        Forest ownLand = new Forest();
        Forest topLand = new Forest();
        Mountain otherLand = new Mountain();
        GrizzlyBears topNonland = new GrizzlyBears();
        GrizzlyBears otherNonland = new GrizzlyBears();

        harness.setExile(player2, List.of(previouslyExiled));
        gd.addToExile(player2.getId(), faceDownLand, null, true);
        harness.setExile(player1, List.of(ownLand));
        harness.setLibrary(player2, List.of(topLand, topNonland, otherLand, otherNonland));
        castSower();

        harness.passBothPriorities();

        PendingInteraction.OblivionSowerLandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.OblivionSowerLandChoice.class);
        assertThat(choice.ownerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds())
                .containsExactly(previouslyExiled.getId(), topLand.getId(), otherLand.getId());

        harness.handleMultipleCardsChosen(player1, List.of(previouslyExiled.getId(), topLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(previouslyExiled.getId(), topLand.getId());
        assertThat(gd.findExiledCard(otherLand.getId())).isNotNull();
        assertThat(gd.findExiledCard(faceDownLand.getId())).isNotNull();
        assertThat(gd.findExiledCard(topNonland.getId())).isNotNull();
        assertThat(gd.findExiledCard(ownLand.getId())).isNotNull();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oblivion Sower");
    }

    @Test
    void mayChooseNoLands() {
        Forest topLand = new Forest();
        harness.setLibrary(player2, List.of(topLand, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castSower();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.OblivionSowerLandChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(topLand.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Forest");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oblivion Sower");
    }

    @Test
    void doesNotPromptWhenNoEligibleLandsExist() {
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, library);
        castSower();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.OblivionSowerLandChoice.class))
                .isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(library.stream().map(Card::getId).toList());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oblivion Sower");
    }

    @Test
    void exilesOnlyTheTopFourCards() {
        Forest first = new Forest();
        Mountain second = new Mountain();
        Forest fifth = new Forest();
        List<Card> topFour = List.of(first, second, new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, List.of(topFour.get(0), topFour.get(1),
                topFour.get(2), topFour.get(3), fifth));
        castSower();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fifth);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(topFour.stream().map(Card::getId).toList());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isFalse());
        harness.assertNotOnBattlefield(player1, "Oblivion Sower");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oblivion Sower");
    }

    @Test
    void exilesAllRemainingCardsFromAShortLibrary() {
        Forest land = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland));
        castSower();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).containsExactly(land.getId(), nonland.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oblivion Sower");
    }

    @Test
    void mayTakePreviouslyExiledLandsEvenWhenTheLibraryIsEmpty() {
        Forest land = new Forest();
        harness.setLibrary(player2, List.of());
        harness.setExile(player2, List.of(land));
        castSower();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.OblivionSowerLandChoice.class)
                .validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(land.getId())).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oblivion Sower");
    }

    @Test
    void enteringWithoutBeingCastDoesNotExileCardsOrOfferLands() {
        Forest land = new Forest();
        List<Card> library = List.of(new Forest(), new GrizzlyBears());
        harness.setLibrary(player2, library);
        harness.setExile(player2, List.of(land));

        harness.enterBattlefieldAndReturn(player1, new OblivionSower());

        harness.assertOnBattlefield(player1, "Oblivion Sower");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.OblivionSowerLandChoice.class))
                .isNull();
    }

    private void castSower() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OblivionSower()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
    }
}
