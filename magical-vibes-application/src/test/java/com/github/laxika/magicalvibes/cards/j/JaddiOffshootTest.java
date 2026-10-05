package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CompleteDisregard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JaddiOffshoot.class, Forest.class, CompleteDisregard.class})
@DisplayName("Jaddi Offshoot")
class JaddiOffshootTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when a land you control enters")
    void gainsLifeOnControllerLandfall() {
        harness.addToBattlefield(player1, new JaddiOffshoot());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's land")
    void doesNotTriggerForOpponentLandfall() {
        harness.addToBattlefield(player1, new JaddiOffshoot());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Offshoot triggers independently for the same land")
    void multipleOffshootsGainLifeIndependently() {
        harness.addToBattlefield(player1, new JaddiOffshoot());
        harness.addToBattlefield(player1, new JaddiOffshoot());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Landfall still gains life after the Offshoot is exiled in response")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        var offshoot = harness.addToBattlefieldAndReturn(player1, new JaddiOffshoot());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new CompleteDisregard()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.castInstant(player2, 0, offshoot.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jaddi Offshoot");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonland creature entering does not cause landfall")
    void nonlandEnteringDoesNotGainLife() {
        harness.addToBattlefield(player1, new JaddiOffshoot());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new JaddiOffshoot(), "{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
