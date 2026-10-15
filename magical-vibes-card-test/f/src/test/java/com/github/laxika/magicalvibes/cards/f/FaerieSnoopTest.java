package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaerieSnoop.class, Island.class, Shock.class})
class FaerieSnoopTest extends BaseCardTest {

    @Test
    void disguiseGivesFaceDownCreatureWard() {
        FaerieSnoop card = new FaerieSnoop();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent snoop = findPermanentForCard(card);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, snoop.getId());
        harness.passBothPriorities();

        assertThat(snoop.isFaceDown()).isTrue();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void turningFaceUpPutsOneOfTheTopTwoCardsIntoHandAndTheOtherIntoGraveyard() {
        Card kept = new Island();
        Card milled = new Shock();
        FaerieSnoop card = new FaerieSnoop();
        harness.setLibrary(player1, List.of(kept, milled));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent snoop = findPermanentForCard(card);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(snoop));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(kept.getId(), milled.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(snoop.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void turningFaceUpWithOneCardPutsItIntoHand() {
        Permanent snoop = castFaceDownSnoop();
        Card onlyCard = new Island();
        harness.setLibrary(player1, List.of(onlyCard));

        turnFaceUpWithBlackMana(snoop);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void turningFaceUpWithEmptyLibraryDoesNotRequireAChoice() {
        Permanent snoop = castFaceDownSnoop();
        harness.setLibrary(player1, List.of());

        turnFaceUpWithBlackMana(snoop);
        harness.passBothPriorities();

        assertThat(snoop.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void payingWardAllowsTheSpellToResolve() {
        Permanent snoop = castFaceDownSnoop();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, snoop.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snoop);
        harness.assertInGraveyard(player1, "Faerie Snoop");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void faceUpSnoopDoesNotHaveDisguiseWard() {
        Permanent snoop = harness.addToBattlefieldAndReturn(player1, new FaerieSnoop());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, snoop.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snoop);
        harness.assertInGraveyard(player2, "Shock");
    }

    private Permanent castFaceDownSnoop() {
        FaerieSnoop card = new FaerieSnoop();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        return findPermanentForCard(card);
    }

    private void turnFaceUpWithBlackMana(Permanent snoop) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(snoop));
    }

    private Permanent findPermanentForCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
