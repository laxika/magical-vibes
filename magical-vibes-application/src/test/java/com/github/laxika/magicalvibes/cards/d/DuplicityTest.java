package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StealEnchantment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Duplicity.class, Disenchant.class, Forest.class, Island.class, StealEnchantment.class})
class DuplicityTest extends BaseCardTest {

    /** Casts Duplicity for player1 and resolves its ETB, returning the permanent id. */
    private UUID castDuplicity() {
        harness.castFromHand(player1, new Duplicity(), "{3}{U}{U}");
        harness.passBothPriorities(); // resolve enchantment → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger
        return harness.getPermanentId(player1, "Duplicity");
    }

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("ETB exiles the top five cards of the controller's library face down with Duplicity")
    void etbExilesTopFiveFaceDown() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest()));
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        UUID permId = castDuplicity();

        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(5);
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
    }

    @Test
    @DisplayName("Accepting the upkeep trigger swaps the hand with the cards exiled with Duplicity")
    void upkeepSwapsHandWithExiledCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest()));
        UUID permId = castDuplicity();
        List<UUID> exiledBefore = gd.getCardsExiledByPermanent(permId).stream().map(Card::getId).toList();

        Card handCard1 = new Island();
        Card handCard2 = new Forest();
        List<UUID> handCardIds = List.of(handCard1.getId(), handCard2.getId());
        harness.setHand(player1, List.of(handCard1, handCard2));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        // The five exiled cards are now in hand; the two hand cards are exiled face down instead.
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(exiledBefore);
        assertThat(gd.getCardsExiledByPermanent(permId))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(handCardIds);
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves hand and exiled cards untouched")
    void upkeepDeclineKeepsEverythingInPlace() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest()));
        UUID permId = castDuplicity();

        Card handCard = new Forest();
        harness.setHand(player1, List.of(handCard));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().extracting(Card::getId).isEqualTo(handCard.getId());
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(5);
    }

    @Test
    @DisplayName("With an empty hand the upkeep trigger still returns the exiled cards")
    void upkeepWithEmptyHandStillReturnsExiledCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest()));
        UUID permId = castDuplicity();

        harness.setHand(player1, List.of());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    @Test
    @DisplayName("The controller's end step forces a discard")
    void endStepDiscardsACard() {
        harness.addToBattlefield(player1, new Duplicity());
        Card discardedCard = new Forest();
        harness.setHand(player1, List.of(discardedCard));

        advanceToEndStepTrigger(player1);
        harness.handleCardChosen(player1, 0); // choose which card to discard

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(discardedCard.getId());
    }

    @Test
    @DisplayName("The opponent's end step does not force the controller to discard")
    void opponentEndStepDoesNotDiscard() {
        harness.addToBattlefield(player1, new Duplicity());
        Card handCard = new Forest();
        harness.setHand(player1, List.of(handCard));

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().extracting(Card::getId).isEqualTo(handCard.getId());
    }

    @Test
    @DisplayName("Duplicity leaving the battlefield puts every card exiled with it into its owner's graveyard")
    void leavingBattlefieldPutsExiledCardsIntoGraveyard() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest()));
        UUID permId = castDuplicity();
        List<UUID> exiledCardIds = gd.getCardsExiledByPermanent(permId).stream()
                .map(Card::getId).toList();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, permId);
        harness.passBothPriorities();
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).noneMatch(e -> permId.equals(e.sourcePermanentId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsAll(exiledCardIds);
    }

    @Test
    @DisplayName("Losing control of Duplicity puts its exiled cards into their owners' graveyards")
    void losingControlPutsExiledCardsIntoGraveyard() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest()));
        UUID permId = castDuplicity();
        List<UUID> exiledCardIds = gd.getCardsExiledByPermanent(permId).stream()
                .map(Card::getId).toList();

        harness.setHand(player2, List.of(new StealEnchantment()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player2, 0, permId);
        harness.passBothPriorities();
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(permId));
        assertThat(gd.exiledCards).noneMatch(e -> permId.equals(e.sourcePermanentId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsAll(exiledCardIds);
    }
}
