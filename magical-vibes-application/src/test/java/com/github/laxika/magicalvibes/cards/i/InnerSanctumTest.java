package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InnerSanctumTest extends BaseCardTest {

    private Permanent addAttacker(UUID controllerId) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(controllerId).add(attacker);
        return attacker;
    }

    private Permanent addBlocker(UUID controllerId, int blockingTarget) {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        gd.playerBattlefields.get(controllerId).add(blocker);
        return blocker;
    }

    @Test
    @DisplayName("Noncombat damage to a creature you control is prevented")
    void preventsNoncombatDamageToYourCreature() {
        harness.addToBattlefield(player1, new InnerSanctum());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage to a creature you control is prevented too")
    void preventsCombatDamage() {
        harness.addToBattlefield(player1, new InnerSanctum());
        Permanent blocker = addBlocker(player1.getId(), 0);
        addAttacker(player2.getId());

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
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, enemyBears.getId());
        harness.passBothPriorities();

        assertThat(enemyBears.getMarkedDamage()).isEqualTo(2);
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
