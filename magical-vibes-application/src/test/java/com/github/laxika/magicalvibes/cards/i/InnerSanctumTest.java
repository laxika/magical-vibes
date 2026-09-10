package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.cards.c.CloudDjinn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnerSanctum.class, AetherFlash.class, CloudDjinn.class})
class InnerSanctumTest extends BaseCardTest {

    private Permanent addAttacker(Player controller) {
        Permanent attacker = addCreatureReady(controller, new CloudDjinn());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker(Player controller, int blockingTarget) {
        Permanent blocker = addCreatureReady(controller, new CloudDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        return blocker;
    }

    @Test
    @DisplayName("Noncombat damage to a creature you control is prevented")
    void preventsNoncombatDamageToYourCreature() {
        harness.addToBattlefield(player1, new InnerSanctum());
        harness.addToBattlefield(player1, new AetherFlash());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new CloudDjinn());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage to a creature you control is prevented too")
    void preventsCombatDamage() {
        harness.addToBattlefield(player1, new InnerSanctum());
        Permanent blocker = addBlocker(player1, 0);
        addAttacker(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage to an opponent's creature is not prevented")
    void doesNotPreventDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new InnerSanctum());
        harness.addToBattlefield(player1, new AetherFlash());
        Permanent enemyCreature = harness.enterBattlefieldAndReturn(player2, new CloudDjinn());
        harness.passBothPriorities();

        assertThat(enemyCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to a player is not prevented")
    void doesNotPreventDamageToPlayer() {
        harness.addToBattlefield(player1, new InnerSanctum());
        addAttacker(player2);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Cumulative upkeep costs 2 life per age counter when paid")
    void cumulativeUpkeepCostsLife() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new InnerSanctum());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(sanctum.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sanctum);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cumulative upkeep costs 2 life for each age counter")
    void cumulativeUpkeepScalesWithAgeCounters() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new InnerSanctum());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(sanctum.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sanctum);
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during its controller's upkeep")
    void cumulativeUpkeepTriggersOnlyDuringControllersUpkeep() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new InnerSanctum());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(sanctum.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sanctum);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices Inner Sanctum")
    void decliningSacrifices() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new InnerSanctum());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sanctum);
        harness.assertLife(player1, 20);
    }
}
