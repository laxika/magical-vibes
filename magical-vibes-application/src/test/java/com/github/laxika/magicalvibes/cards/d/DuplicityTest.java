package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicityTest extends BaseCardTest {

    /** Casts Duplicity for player1 and resolves its ETB, returning the permanent id. */
    private UUID castDuplicity() {
        harness.setHand(player1, new ArrayList<>(List.of(new Duplicity())));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger
        return harness.getPermanentId(player1, "Duplicity");
    }

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger onto stack
        harness.passBothPriorities(); // resolve trigger
    }

    @Test
    @DisplayName("ETB exiles the top five cards of the controller's library face down with Duplicity")
    void etbExilesTopFiveFaceDown() {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 6; i++) gd.playerDecks.get(player1.getId()).add(new Forest());
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
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 5; i++) gd.playerDecks.get(player1.getId()).add(new Forest());
        UUID permId = castDuplicity();
        List<UUID> exiledBefore = gd.getCardsExiledByPermanent(permId).stream().map(Card::getId).toList();

        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new LlanowarElves())));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        // The five exiled cards are now in hand; the two hand cards are exiled face down instead.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(c -> exiledBefore.contains(c.getId()));
        assertThat(gd.getCardsExiledByPermanent(permId))
                .hasSize(2)
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves");
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves hand and exiled cards untouched")
    void upkeepDeclineKeepsEverythingInPlace() {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 5; i++) gd.playerDecks.get(player1.getId()).add(new Forest());
        UUID permId = castDuplicity();

        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().matches(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(5);
    }

    @Test
    @DisplayName("With an empty hand the upkeep trigger still returns the exiled cards")
    void upkeepWithEmptyHandStillReturnsExiledCards() {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 5; i++) gd.playerDecks.get(player1.getId()).add(new Forest());
        UUID permId = castDuplicity();

        harness.setHand(player1, new ArrayList<>());

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
        harness.setHand(player1, new ArrayList<>(List.of(new LlanowarElves())));

        advanceToEndStepTrigger(player1);
        harness.handleCardChosen(player1, 0); // choose which card to discard

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("The opponent's end step does not force the controller to discard")
    void opponentEndStepDoesNotDiscard() {
        harness.addToBattlefield(player1, new Duplicity());
        harness.setHand(player1, new ArrayList<>(List.of(new LlanowarElves())));

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Duplicity leaving the battlefield puts every card exiled with it into its owner's graveyard")
    void leavingBattlefieldPutsExiledCardsIntoGraveyard() {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 5; i++) gd.playerDecks.get(player1.getId()).add(new Forest());
        UUID permId = castDuplicity();

        harness.setHand(player2, new ArrayList<>(List.of(new Disenchant())));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, permId);
        harness.passBothPriorities();
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).noneMatch(e -> permId.equals(e.sourcePermanentId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Forest"))
                .hasSize(5);
    }
}
