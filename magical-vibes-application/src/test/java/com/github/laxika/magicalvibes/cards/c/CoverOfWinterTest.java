package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoverOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents age-counter damage from combat creatures to you and your creatures")
    void preventsCombatDamageToControllerAndCreatures() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 1);
        Permanent blocker = addReadyCreature(player1);
        Permanent blockedAttacker = addAttacker(player2);
        Permanent unblockedAttacker = addAttacker(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blockedAttacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unblockedAttacker);
    }

    @Test
    @DisplayName("Prevents damage equal to all age counters")
    void scalesWithAgeCounters() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 2);
        addAttacker(player2);

        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        cover.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The snow activation adds an age counter")
    void snowActivationAddsAgeCounter() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(cover.getCounterCount(CounterType.AGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cumulative upkeep puts on an age counter and charges one snow mana per counter")
    void cumulativeUpkeepUsesSnowManaPerAgeCounter() {
        Permanent cover = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(cover.getCounterCount(CounterType.AGE)).isEqualTo(1);

        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cover);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addReadyCreature(player);
        attacker.setAttacking(true);
        return attacker;
    }
}
