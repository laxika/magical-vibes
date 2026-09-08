package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MortalCombat;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeraldOfEternalDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Controller does not lose at 0 life")
    void controllerDoesNotLoseAtZeroLife() {
        harness.addToBattlefield(player1, new HeraldOfEternalDawn());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Opponent cannot win the game")
    void opponentCannotWinTheGame() {
        harness.addToBattlefield(player1, new HeraldOfEternalDawn());
        harness.addToBattlefield(player2, new MortalCombat());

        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            graveyard.add(new GrizzlyBears());
        }
        harness.setGraveyard(player2, graveyard);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
