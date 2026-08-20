package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElspethStormSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates two 1/1 white Soldier tokens")
    void plusOneCreatesDoubledSoldierTokens() {
        Permanent elspeth = addReadyElspeth(player1, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    @DisplayName("0 puts counters on controlled creatures and grants flying until the next turn")
    void zeroCountersCreaturesAndGrantsFlying() {
        Permanent elspeth = addReadyElspeth(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("0's flying grant expires at the start of the controller's next turn")
    void zeroFlyingGrantEndsOnNextTurn() {
        addReadyElspeth(player1, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        endTurn(player1);
        endTurn(player2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("-3 destroys an opponent's creature with mana value 3 or greater")
    void minusThreeDestroysLargeOpponentCreature() {
        Permanent elspeth = addReadyElspeth(player1, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("-3 rejects creatures with mana value less than 3 and creatures you control")
    void minusThreeRejectsIllegalTargets() {
        addReadyElspeth(player1, 5);
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownLargeCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, smallCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, ownLargeCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, java.util.List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }

    private Permanent addReadyElspeth(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ElspethStormSlayer());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
