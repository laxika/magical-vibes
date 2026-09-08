package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.InvasionOfKamigawa;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtchedHostDoombringer.class, InvasionOfKamigawa.class})
class EtchedHostDoombringerTest extends BaseCardTest {

    @Test
    void lifeModeMakesTargetOpponentLoseLifeAndControllerGainLife() {
        cast(0, player2.getId());
        resolveCreatureAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void battleModeRemovesDefenseCountersWhenAnOpponentProtectsTheBattle() {
        Permanent battle = addBattle(player2, player2.getId(), 5);

        cast(1, battle.getId());
        resolveCreatureAndTrigger();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    void battleModePutsDefenseCountersOnABattleProtectedByTheController() {
        Permanent battle = addBattle(player2, player1.getId(), 2);

        cast(1, battle.getId());
        resolveCreatureAndTrigger();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
    }

    private Permanent addBattle(com.github.laxika.magicalvibes.model.Player controller,
                                java.util.UUID protectorId, int defenseCounters) {
        Permanent battle = harness.addToBattlefieldAndReturn(controller, new InvasionOfKamigawa());
        battle.setProtectorPlayerId(protectorId);
        battle.setCounterCount(CounterType.DEFENSE, defenseCounters);
        return battle;
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new EtchedHostDoombringer()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, mode, targetId);
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
