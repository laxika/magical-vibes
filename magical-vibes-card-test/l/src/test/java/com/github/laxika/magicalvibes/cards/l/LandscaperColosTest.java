package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RiptideLaboratory;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LandscaperColos.class, Forest.class, GrizzlyBears.class, Shock.class, RiptideLaboratory.class})
class LandscaperColosTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from an opponent's graveyard on the bottom of its owner's library")
    void putsOpponentGraveyardCardOnLibraryBottom() {
        Card target = new GrizzlyBears();
        Card existingTop = new Shock();
        Card existingBottom = new Shock();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existingTop, existingBottom));
        harness.castFromHand(player1, new LandscaperColos(), "{5}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, existingBottom, target);
    }

    @Test
    @DisplayName("Cannot target a card in the controller's graveyard")
    void cannotTargetOwnGraveyard() {
        Card ownTarget = new GrizzlyBears();
        Card opponentTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownTarget));
        harness.setGraveyard(player2, List.of(opponentTarget));
        harness.castFromHand(player1, new LandscaperColos(), "{5}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentTarget.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basic landcycling discards Landscaper Colos and searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new LandscaperColos()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Landscaper Colos");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canPutAnOpponentLandCardOnLibraryBottom() {
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new LandscaperColos(), "{5}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target);
    }

    @Test
    void entersWhenOpponentGraveyardIsEmpty() {
        Card ownCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of());

        harness.castFromHand(player1, new LandscaperColos(), "{5}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Landscaper Colos");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotChooseAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        Card otherCard = new Shock();
        Card libraryCard = new Forest();
        harness.setGraveyard(player2, List.of(target, otherCard));
        harness.setLibrary(player2, List.of(libraryCard));

        harness.castFromHand(player1, new LandscaperColos(), "{5}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(otherCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void basicLandcyclingCanFailToFindEvenWithABasicLandAvailable() {
        Card colos = new LandscaperColos();
        Card forest = new Forest();
        Card nonbasicLand = new RiptideLaboratory();
        harness.setHand(player1, List.of(colos));
        harness.setLibrary(player1, List.of(forest, nonbasicLand));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(colos);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, nonbasicLand);

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, nonbasicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void basicLandcyclingResolvesWhenNoBasicLandIsAvailable() {
        Card nonbasicLand = new RiptideLaboratory();
        harness.setHand(player1, List.of(new LandscaperColos()));
        harness.setLibrary(player1, List.of(nonbasicLand));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Landscaper Colos");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
