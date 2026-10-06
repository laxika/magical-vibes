package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SheoldredsAssimilator.class, GrizzlyBears.class})
class SheoldredsAssimilatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a graveyard card and may conjure its duplicate into the top five")
    void etbExilesAndConjuresDuplicate() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        List<Card> originalLibrary = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, originalLibrary);
        harness.castFromHand(player1, new SheoldredsAssimilator(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(graveyardCard.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        int duplicateIndex = java.util.stream.IntStream.range(0, gd.playerDecks.get(player1.getId()).size())
                .filter(index -> !originalLibrary.contains(gd.playerDecks.get(player1.getId()).get(index)))
                .findFirst().orElseThrow();
        assertThat(duplicateIndex).isLessThan(5);
        assertThat(gd.playerDecks.get(player1.getId()).get(duplicateIndex).getId())
                .isNotEqualTo(graveyardCard.getId());
    }

    @Test
    @DisplayName("Declining the duplicate leaves the targeted card exiled")
    void etbMayBeDeclined() {
        Card graveyardCard = new GrizzlyBears();
        Card libraryCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new SheoldredsAssimilator(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Attacking offers the same optional graveyard exile")
    void attackTriggersTheAbility() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        Permanent assimilator = harness.addToBattlefieldAndReturn(player1, new SheoldredsAssimilator());
        assimilator.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(graveyardCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    @DisplayName("Choosing no graveyard target does not exile or offer to conjure")
    void mayChooseNoTarget() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SheoldredsAssimilator(), "{1}{B}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.findExiledCard(graveyardCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Empty graveyards require no choice and conjure nothing")
    void emptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SheoldredsAssimilator(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A target that leaves its graveyard before resolution is not duplicated")
    void targetLeavesGraveyardBeforeResolution() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SheoldredsAssimilator(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(graveyardCard));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    @DisplayName("Conjuring into a short library preserves its existing cards in order")
    void conjuresIntoShortLibrary() {
        Card graveyardCard = new GrizzlyBears();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card opponentLibraryCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.castFromHand(player1, new SheoldredsAssimilator(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.stream().filter(card -> card == first || card == second).toList())
                .containsExactly(first, second);
        assertThat(library).noneMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
    }

    @Test
    @DisplayName("A duplicate conjured into an empty library becomes a nontoken creature when cast")
    void conjuredDuplicateIsNotATokenPermanent() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new SheoldredsAssimilator(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerDecks.get(player1.getId()).getFirst();
        assertThat(duplicate.getId()).isNotEqualTo(graveyardCard.getId());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, duplicate, "{1}{G}");
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(duplicate.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isToken(gd, permanent)).isFalse();
    }
}
