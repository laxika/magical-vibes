package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorinMarkovTest extends BaseCardTest {

    @Test
    @DisplayName("+2 deals 2 damage to target player and gains 2 life")
    void plusTwoDamagesPlayerAndGainsLife() {
        Permanent sorin = addReadySorin(player1);
        gd.playerLifeTotals.put(player1.getId(), 15);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("+2 can deal 2 damage to a creature and still gains 2 life")
    void plusTwoDamagesCreature() {
        addReadySorin(player1);
        gd.playerLifeTotals.put(player1.getId(), 15);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("-3 sets target opponent's life total to 10")
    void minusThreeSetsOpponentLifeToTen() {
        Permanent sorin = addReadySorin(player1);
        gd.playerLifeTotals.put(player2.getId(), 30);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("-3 raises a low opponent's life total up to 10")
    void minusThreeRaisesLowLifeTotal() {
        addReadySorin(player1);
        gd.playerLifeTotals.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("-3 cannot target its controller")
    void minusThreeRejectsController() {
        addReadySorin(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 gives control of target player's next turn to Sorin's controller")
    void minusSevenTakesControlOfNextTurn() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("-7 cannot be activated with only starting loyalty")
    void minusSevenRequiresSevenLoyalty() {
        addReadySorin(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("loyalty");
    }

    private Permanent addReadySorin(Player player) {
        Permanent perm = new Permanent(new SorinMarkov());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
