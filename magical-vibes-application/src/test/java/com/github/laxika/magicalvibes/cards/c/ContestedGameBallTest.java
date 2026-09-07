package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContestedGameBall.class, GrizzlyBears.class})
class ContestedGameBallTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage transfers control and untaps the ball")
    void combatDamageTransfersControlAndUntaps() {
        Permanent ball = addBall(player2);
        ball.tap();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1, player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ball);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ball);
        assertThat(ball.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability draws and adds a point counter")
    void activatedAbilityDrawsAndAddsPointCounter() {
        Permanent ball = addBall(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(ball.getCounterCount(CounterType.POINT)).isEqualTo(1);
        assertThat(ball.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The fifth point counter sacrifices the ball and creates a Treasure")
    void fifthPointCounterSacrificesAndCreatesTreasure() {
        Permanent ball = addBall(player1);
        ball.setCounterCount(CounterType.POINT, 4);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ball);
        harness.assertInGraveyard(player1, "Contested Game Ball");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
    }

    private Permanent addBall(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ContestedGameBall());
    }

    private void resolveCombat(Player attacker, Player defender) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
