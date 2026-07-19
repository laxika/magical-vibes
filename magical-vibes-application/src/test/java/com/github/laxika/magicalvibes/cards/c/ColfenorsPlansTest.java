package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColfenorsPlansTest extends BaseCardTest {

    // ===== ETB — exile top 7 of controller's library =====

    @Test
    @DisplayName("ETB exiles the top 7 cards of the controller's library (only) to itself")
    void etbExilesTopSevenOfControllersLibrary() {
        gd.playerDecks.get(player1.getId()).clear();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 8; i++) library.add(new Forest());
        gd.playerDecks.get(player1.getId()).addAll(library);

        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ColfenorsPlans()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Colfenor's Plans");

        UUID permId = harness.getPermanentId(player1, "Colfenor's Plans");
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(7);
        assertThat(gd.exiledCards).filteredOn(e -> permId.equals(e.sourcePermanentId()))
                .allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        // Opponent's library is untouched (this exiles only the controller's cards).
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
    }

    // ===== Skip your draw step =====

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.addToBattlefield(player1, new ColfenorsPlans());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid the first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance UPKEEP → DRAW, runs handleDrawStep

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    // ===== Can't cast more than one spell each turn (controller only) =====

    @Test
    @DisplayName("Controller can't cast a second spell in a turn")
    void controllerLimitedToOneSpell() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent is not restricted by Colfenor's Plans")
    void opponentNotRestricted() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        // Second spell is fine for the opponent.
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Play lands and cast spells from among the exiled cards =====

    @Test
    @DisplayName("Controller may play a land and cast a spell from the exiled cards")
    void playsAndCastsFromExile() {
        harness.addToBattlefield(player1, new ColfenorsPlans());
        UUID permId = harness.getPermanentId(player1, "Colfenor's Plans");

        Card exiledLand = new Forest();
        Card exiledCreature = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledLand, permId);
        gd.addToExile(player1.getId(), exiledCreature, permId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play the land from exile — no longer in exile, now on battlefield.
        gs.playCardFromExile(gd, player1, exiledLand.getId(), null, null);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getCardsExiledByPermanent(permId))
                .noneMatch(c -> c.getId().equals(exiledLand.getId()));

        // Cast the creature from exile — put it on the stack and resolve it.
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCardFromExile(gd, player1, exiledCreature.getId(), null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
