package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.InvasionOfKamigawa;
import com.github.laxika.magicalvibes.cards.r.RooftopSaboteurs;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtchedHostDoombringer.class, InvasionOfKamigawa.class, RooftopSaboteurs.class})
class EtchedHostDoombringerTest extends BaseCardTest {

    private static final String LIFE_MODE = "Target opponent loses 2 life and you gain 2 life";
    private static final String BATTLE_MODE =
            "Choose target battle. If an opponent protects it, remove three defense counters from it. Otherwise, put three defense counters on it";

    @Test
    @DisplayName("The life mode drains target opponent for 2")
    void lifeModeMakesTargetOpponentLoseLifeAndControllerGainLife() {
        cast(LIFE_MODE, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The battle mode removes three defense counters from a battle an opponent protects")
    void battleModeRemovesDefenseCountersWhenAnOpponentProtectsTheBattle() {
        Permanent battle = addBattle(player1, player2.getId(), 5);

        cast(BATTLE_MODE, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The battle mode puts three defense counters on a battle its controller protects")
    void battleModePutsDefenseCountersOnABattleProtectedByTheController() {
        Permanent battle = addBattle(player2, player1.getId(), 2);

        cast(BATTLE_MODE, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing the last defense counters defeats the Siege")
    void removingTheLastDefenseCountersDefeatsTheSiege() {
        Permanent battle = addBattle(player1, player2.getId(), 2);

        cast(BATTLE_MODE, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isZero();
        harness.assertOnBattlefield(player1, "Invasion of Kamigawa");
        harness.assertNotInGraveyard(player1, "Invasion of Kamigawa");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rooftop Saboteurs");
        harness.assertNotOnBattlefield(player1, "Invasion of Kamigawa");
    }

    @Test
    @DisplayName("The battle mode can be chosen when the Doombringer enters without being cast")
    void battleModeCanBeChosenWhenDoombringerEntersWithoutBeingCast() {
        Permanent battle = addBattle(player1, player2.getId(), 5);

        harness.enterBattlefieldAndReturn(player1, new EtchedHostDoombringer());
        harness.inMutationScope(() -> harness.getTriggeredAbilityQueueService().processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, BATTLE_MODE);
        harness.handlePermanentChosen(player1, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The battle mode reads the protector as it resolves")
    void battleModeChecksTheProtectorAtResolution() {
        Permanent battle = addBattle(player2, player1.getId(), 5);
        cast(BATTLE_MODE, battle.getId());

        gd.playerBattlefields.get(player2.getId()).remove(battle);
        gd.playerBattlefields.get(player1.getId()).add(battle);
        battle.setProtectorPlayerId(player2.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The battle mode does nothing when its target left before it resolves")
    void battleModeDoesNothingWhenTheTargetLeavesBeforeResolution() {
        Permanent battle = addBattle(player1, player2.getId(), 5);
        cast(BATTLE_MODE, battle.getId());

        gd.playerBattlefields.get(player1.getId()).remove(battle);
        gd.playerGraveyards.get(player1.getId()).add(battle.getCard());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(5);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The life mode resolves after the Doombringer left")
    void lifeModeStillResolvesWhenDoombringerLeavesBeforeItsTrigger() {
        cast(LIFE_MODE, player2.getId());

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

    private void cast(String mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new EtchedHostDoombringer()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.handlePermanentChosen(player1, targetId);
    }
}
