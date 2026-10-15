package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Glitterfang.class)
class GlitterfangTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Returns itself to its owner's hand at the end step")
    void returnsItselfAtEndStep() {
        Glitterfang fang = new Glitterfang();
        harness.addToBattlefield(player1, fang);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glitterfang");
        assertThat(gd.playerHands.get(player1.getId())).contains(fang);
    }

    @Test
    @DisplayName("Triggers during an opponent's end step")
    void triggersDuringOpponentsEndStep() {
        Glitterfang fang = new Glitterfang();
        harness.addToBattlefield(player1, fang);

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glitterfang");
        assertThat(gd.playerHands.get(player1.getId())).contains(fang);
    }

    @Test
    @DisplayName("Returns itself to its owner's hand when controlled by another player")
    void returnsItselfToOwnersHandWhenControlledByAnotherPlayer() {
        Glitterfang fang = new Glitterfang();
        fang.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, fang);

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glitterfang");
        assertThat(gd.playerHands.get(player1.getId())).contains(fang);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(fang);
    }

    @Test
    @DisplayName("Can attack on the turn it enters the battlefield")
    void canAttackOnTurnItEnters() {
        var fang = harness.addToBattlefieldAndReturn(player1, new Glitterfang());
        fang.setSummoningSick(true);

        assertThat(als.canAttack(gd, fang, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Each Glitterfang returns only itself")
    void eachCopyReturnsOnlyItself() {
        Glitterfang first = new Glitterfang();
        Glitterfang second = new Glitterfang();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player2, second);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glitterfang");
        harness.assertNotOnBattlefield(player2, "Glitterfang");
        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerHands.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    @DisplayName("A Glitterfang entering after the end step begins does not return that step")
    void enteringAfterEndStepBeginsDoesNotTrigger() {
        Glitterfang first = new Glitterfang();
        Glitterfang lateArrival = new Glitterfang();
        harness.addToBattlefield(player1, first);

        advanceToEndStep(player1);
        harness.enterBattlefieldAndReturn(player1, lateArrival);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(lateArrival);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard()).containsExactly(lateArrival);
    }
}
