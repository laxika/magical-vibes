package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.InvasionOfKamigawa;
import com.github.laxika.magicalvibes.cards.r.RooftopSaboteurs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtchedHostDoombringer.class, InvasionOfKamigawa.class, RooftopSaboteurs.class})
class EtchedHostDoombringerTest extends BaseCardTest {

    @Test
    void lifeModeMakesTargetOpponentLoseLifeAndControllerGainLife() {
        cast(0, player2.getId());
        resolveCreatureAndTrigger();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void battleModeRemovesDefenseCountersWhenAnOpponentProtectsTheBattle() {
        Permanent battle = addBattle(player1, player2.getId(), 5);

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

    @Test
    void removingTheLastDefenseCountersDefeatsTheSiege() {
        Permanent battle = addBattle(player1, player2.getId(), 2);

        cast(1, battle.getId());
        resolveCreatureAndTrigger();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isZero();
        harness.assertOnBattlefield(player1, "Invasion of Kamigawa");
        harness.assertNotInGraveyard(player1, "Invasion of Kamigawa");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rooftop Saboteurs");
        harness.assertNotOnBattlefield(player1, "Invasion of Kamigawa");
    }

    @Test
    void battleModeCanBeChosenWhenDoombringerEntersWithoutBeingCast() {
        Permanent battle = addBattle(player1, player2.getId(), 5);

        harness.enterBattlefieldAndReturn(player1, new EtchedHostDoombringer());
        harness.handleListChoice(player1,
                "Choose target battle. If an opponent protects it, remove three defense counters from it. Otherwise, put three defense counters on it");
        harness.handlePermanentChosen(player1, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void battleModeChecksTheProtectorAtResolution() {
        Permanent battle = addBattle(player2, player1.getId(), 5);
        cast(1, battle.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(battle);
        gd.playerBattlefields.get(player1.getId()).add(battle);
        battle.setProtectorPlayerId(player2.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    void battleModeDoesNothingWhenTheTargetLeavesBeforeResolution() {
        Permanent battle = addBattle(player1, player2.getId(), 5);
        cast(1, battle.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(battle);
        gd.playerGraveyards.get(player1.getId()).add(battle.getCard());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void lifeModeStillResolvesWhenDoombringerLeavesBeforeItsTrigger() {
        cast(0, player2.getId());
        harness.passBothPriorities();

        Permanent doombringer = findPermanent(player1, "Etched Host Doombringer");
        gd.playerBattlefields.get(player1.getId()).remove(doombringer);
        gd.playerGraveyards.get(player1.getId()).add(doombringer.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
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
