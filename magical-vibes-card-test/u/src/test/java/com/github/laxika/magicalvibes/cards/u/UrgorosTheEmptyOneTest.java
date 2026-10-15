package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CloudreaderSphinx;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrgorosTheEmptyOne.class, CloudreaderSphinx.class, TamiyoCollectorOfTales.class})
class UrgorosTheEmptyOneTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage forces opponent to discard a card at random when they have cards")
    void combatDamageTriggersRandomDiscard() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new CloudreaderSphinx()));
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        Permanent urgoros = addCreatureReady(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Cloudreader Sphinx");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("discards") && log.contains("at random"));
        // Controller should NOT draw when opponent discarded
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize);
    }

    @Test
    @DisplayName("Discards one card when opponent has multiple cards in hand")
    void discardsOneCardFromMultiple() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new CloudreaderSphinx(), new CloudreaderSphinx(), new CloudreaderSphinx()));

        Permanent urgoros = addCreatureReady(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Cloudreader Sphinx");
    }

    @Test
    @DisplayName("Controller draws a card when opponent has empty hand")
    void drawsCardWhenOpponentHandEmpty() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of());
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        Permanent urgoros = addCreatureReady(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize + 1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("No trigger when Urgoros is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new CloudreaderSphinx()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        Permanent urgoros = addCreatureReady(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);
        // A flying blocker can legally block Urgoros.
        Permanent blocker = addCreatureReady(player2, new CloudreaderSphinx());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize);
    }

    @Test
    @DisplayName("Defender takes 4 combat damage from unblocked Urgoros")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new CloudreaderSphinx()));

        Permanent urgoros = addCreatureReady(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Game advances after trigger resolves")
    void gameAdvancesAfterTrigger() {
        harness.setHand(player2, List.of(new CloudreaderSphinx()));

        Permanent urgoros = addCreatureReady(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("An empty hand at resolution causes a draw even if the player had a card when damaged")
    void checksEmptyHandAtResolution() {
        harness.setHand(player2, List.of(new CloudreaderSphinx()));
        harness.setLibrary(player1, List.of(new CloudreaderSphinx()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        addCreatureReady(player1, new UrgorosTheEmptyOne()).setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("A card acquired before resolution is discarded instead of drawing")
    void checksNonemptyHandAtResolution() {
        harness.setHand(player2, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();
        addCreatureReady(player1, new UrgorosTheEmptyOne()).setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new CloudreaderSphinx()));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Cloudreader Sphinx");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("The controller of Urgoros draws when the other player has no cards")
    void secondPlayerControllerDraws() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(new CloudreaderSphinx()));
        int handSize = gd.playerHands.get(player2.getId()).size();
        addCreatureReady(player2, new UrgorosTheEmptyOne()).setAttacking(true);

        resolveCombat(player2);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controller draws when Tamiyo prevents the damaged player from discarding")
    void drawsWhenDiscardIsProhibited() {
        harness.setHand(player2, List.of(new CloudreaderSphinx()));
        harness.setLibrary(player1, List.of(new CloudreaderSphinx()));
        harness.addToBattlefield(player2, new TamiyoCollectorOfTales());
        int handSize = gd.playerHands.get(player1.getId()).size();
        addCreatureReady(player1, new UrgorosTheEmptyOne()).setAttacking(true);

        resolveCombat();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }
}
