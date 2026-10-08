package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarrierPigeons.class})
class CarrierPigeonsTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void keepTurnStepsAvailableForAssertions() {
        for (var player : java.util.List.of(player1, player2)) {
            gd.playerAutoStopSteps.put(player.getId(), java.util.EnumSet.of(
                    com.github.laxika.magicalvibes.model.TurnStep.UPKEEP,
                    com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                    com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE));
        }
    }


    @Test
    @DisplayName("ETB trigger schedules a draw at the next upkeep without drawing immediately")
    void etbSchedulesDelayedDraw() {
        harness.setHand(player1, List.of(new CarrierPigeons()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.setHand(player1, List.of(new CarrierPigeons()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("The draw survives the source dying before its entry trigger resolves")
    void drawSurvivesSourceLeaving() {
        harness.forceActivePlayer(player1);
        Permanent pigeons = harness.enterBattlefieldAndReturn(player1, new CarrierPigeons());
        assertThat(gd.stack).hasSize(1);

        pigeons.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Carrier Pigeons");
        resolveAllTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    @DisplayName("Each copy schedules its own draw and the draws do not repeat")
    void multipleCopiesDrawOnlyOnce() {
        harness.forceActivePlayer(player1);
        harness.enterBattlefieldAndReturn(player1, new CarrierPigeons());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new CarrierPigeons());
        resolveAllTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("An additional upkeep this turn does not draw, but the controller's next turn does")
    void waitsForNextTurnEvenWhenControllerTakesConsecutiveTurns() {
        harness.forceActivePlayer(player1);
        harness.enterBattlefieldAndReturn(player1, new CarrierPigeons());
        resolveAllTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        gd.turnNumber++;
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
