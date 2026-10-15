package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DictateOfKruphix.class})
class DictateOfKruphixTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Each player draws an additional card during their draw step")
    void triggersDrawForTheActivePlayer() {
        harness.addToBattlefield(player1, new DictateOfKruphix());
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
    }

    @Test
    void controllerDrawsNormallyBeforeTheAdditionalDrawResolves() {
        harness.addToBattlefield(player1, new DictateOfKruphix());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    void copiesControlledByDifferentPlayersEachAddADraw() {
        harness.addToBattlefield(player1, new DictateOfKruphix());
        harness.addToBattlefield(player2, new DictateOfKruphix());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
    }

    @Test
    void additionalDrawStillResolvesAfterTheEnchantmentLeaves() {
        harness.addToBattlefield(player1, new DictateOfKruphix());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    void canBeCastDuringOpponentsUpkeepAndTriggersInTheFollowingDrawStep() {
        harness.setHand(player1, List.of(new DictateOfKruphix()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.castEnchantment(player1, 0);
        harness.withAutoStop(TurnStep.UPKEEP, this::resolveAllTriggers);

        assertThat(countPermanents(player1, "Dictate of Kruphix")).isEqualTo(1);

        harness.passUntil(player2, TurnStep.DRAW);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    void enteringDuringDrawStepDoesNotTriggerRetroactively() {
        advanceToDraw(player2);
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new DictateOfKruphix()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0);
        harness.withAutoStop(TurnStep.DRAW, this::resolveAllTriggers);

        assertThat(countPermanents(player1, "Dictate of Kruphix")).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }
}
