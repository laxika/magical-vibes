package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishCannoneers.class, GarrukWildspeaker.class, GrizzlyBears.class})
class OrcishCannoneersTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target player and 3 damage to controller")
    void deals2ToPlayerAnd3ToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new OrcishCannoneers());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 2/2, and 3 damage to controller")
    void deals2ToCreatureAnd3ToController() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addCreatureReady(player1, new OrcishCannoneers());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 2 damage to a planeswalker and 3 damage to controller")
    void deals2ToPlaneswalkerAnd3ToController() {
        harness.setLife(player1, 20);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        addCreatureReady(player1, new OrcishCannoneers());

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target the controller and deals both damage amounts to that player")
    void canTargetController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new OrcishCannoneers());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Activating taps the creature")
    void activatingTaps() {
        Permanent cannoneers = addCreatureReady(player1, new OrcishCannoneers());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(cannoneers.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new OrcishCannoneers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Ability fizzles if target creature leaves — controller takes no damage")
    void fizzlesIfTargetRemoved() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new OrcishCannoneers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ability still deals both amounts after its source leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new OrcishCannoneers());

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself without preventing the damage to its controller")
    void canTargetItself() {
        harness.setLife(player1, 20);
        Permanent cannoneers = addCreatureReady(player1, new OrcishCannoneers());

        harness.activateAbility(player1, 0, null, cannoneers.getId());
        harness.passBothPriorities();

        assertThat(cannoneers.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Orcish Cannoneers");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Both players lose together when both damage amounts are lethal")
    void lethalDamageToBothPlayersDraws() {
        harness.setLife(player1, 3);
        harness.setLife(player2, 2);
        addCreatureReady(player1, new OrcishCannoneers());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 0);
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.DRAW);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new OrcishCannoneers());
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }
}
