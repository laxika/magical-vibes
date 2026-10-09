package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TidalWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadpoolTradingCard.class, TidalWarrior.class})
class DeadpoolTradingCardTest extends BaseCardTest {

    private void castAndChoose(Permanent target, boolean accept) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DeadpoolTradingCard(), "{2}{B}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handleMayAbilityChosen(player1, accept);
    }

    @Test
    @DisplayName("Exchanging text boxes transfers the upkeep life loss to the other controller")
    void exchangesTextBoxes() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent tidalWarrior = harness.addToBattlefieldAndReturn(player2, new TidalWarrior());
        castAndChoose(tidalWarrior, true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Deadpool, Trading Card");
        harness.assertOnBattlefield(player2, "Tidal Warrior");
    }

    @Test
    @DisplayName("Declining the exchange preserves Deadpool's upkeep life loss")
    void declinesExchange() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent tidalWarrior = harness.addToBattlefieldAndReturn(player1, new TidalWarrior());
        castAndChoose(tidalWarrior, false);
        harness.setLife(player1, 20);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Copied Deadpool sacrifice ability makes each other player draw")
    void copiedSacrificeAbilityWorks() {
        Permanent tidalWarrior = harness.addToBattlefieldAndReturn(player1, new TidalWarrior());
        castAndChoose(tidalWarrior, true);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int battlefieldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tidalWarrior);
        harness.activateAbility(player1, battlefieldIndex, null, null);
        harness.assertNotOnBattlefield(player1, "Tidal Warrior");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
    }

    @Test
    @DisplayName("Text exchange is an entry choice rather than a triggered ability on the stack")
    void exchangeDoesNotUseTheStack() {
        Permanent tidalWarrior = harness.addToBattlefieldAndReturn(player2, new TidalWarrior());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DeadpoolTradingCard(), "{2}{B}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, tidalWarrior.getId());
        assertThat(gd.stack).isEmpty();
    }
}
