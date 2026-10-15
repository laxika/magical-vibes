package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WaryWatchdog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreatsAroundEveryCorner.class, Forest.class, WaryWatchdog.class})
class ThreatsAroundEveryCornerTest extends BaseCardTest {

    @Test
    void manifestsDreadAndSearchesForABasicLandWhenTheManifestedPermanentEnters() {
        Card manifestedCard = new WaryWatchdog();
        Card graveyardCard = new WaryWatchdog();
        Card searchedLand = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard, searchedLand));
        harness.castFromHand(player1, new ThreatsAroundEveryCorner(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(searchedLand);

        harness.handleCardChosen(player1, 0);

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCard()).isEqualTo(manifestedCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(searchedLand) && permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotManifestOrTriggerALandSearch() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new ThreatsAroundEveryCorner(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void singleLibraryCardIsManifestedEvenWhenItIsALand() {
        Card manifestedCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard));
        harness.castFromHand(player1, new ThreatsAroundEveryCorner(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard);
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(manifestedCard)
                        && permanent.isFaceDown() && permanent.isManifested());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayFailToFindEvenWhenABasicLandIsAvailable() {
        Card manifestedCard = new Forest();
        Card graveyardCard = new Forest();
        Card availableLand = new Forest();
        Card nonland = new WaryWatchdog();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard, availableLand, nonland));
        harness.castFromHand(player1, new ThreatsAroundEveryCorner(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(availableLand);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(availableLand, nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void faceUpPermanentEnteringDoesNotTriggerALandSearch() {
        harness.addToBattlefield(player1, new ThreatsAroundEveryCorner());
        Card libraryLand = new Forest();
        harness.setLibrary(player1, List.of(libraryLand));

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
    }

    @Test
    void opponentsFaceDownPermanentDoesNotTriggerYourEnchantment() {
        harness.addToBattlefield(player1, new ThreatsAroundEveryCorner());
        Card yourLand = new Forest();
        harness.setLibrary(player1, List.of(yourLand));
        Card opponentsManifest = new Forest();
        Card opponentsGraveyardCard = new Forest();
        Card opponentsLand = new Forest();
        harness.setLibrary(player2, List.of(opponentsManifest, opponentsGraveyardCard, opponentsLand));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ThreatsAroundEveryCorner(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(opponentsManifest.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(yourLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(opponentsLand) && permanent.isTapped());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void eachCopyTriggersSeparatelyForTheSameFaceDownPermanent() {
        harness.addToBattlefield(player1, new ThreatsAroundEveryCorner());
        Card manifestedCard = new Forest();
        Card graveyardCard = new Forest();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard, firstLand, secondLand));
        harness.castFromHand(player1, new ThreatsAroundEveryCorner(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(firstLand) && permanent.isTapped())
                .anyMatch(permanent -> permanent.getCard().equals(secondLand) && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void turningAManifestedCreatureFaceUpDoesNotTriggerAnotherLandSearch() {
        Card manifestedCard = new WaryWatchdog();
        Card graveyardCard = new Forest();
        Card availableLand = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard, availableLand));
        harness.castFromHand(player1, new ThreatsAroundEveryCorner(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(manifested);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.turnFaceUp(player1, permanentIndex);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(availableLand);
    }
}
