package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWanderer.class, CrawWurm.class, GrizzlyBears.class, Shock.class})
class TheWandererTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage to its controller and permanents they control")
    void preventsNoncombatDamageToControllerAndPermanents() {
        addReadyWanderer(player1, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        castShock(player2, player1.getId());
        castShock(player2, bears.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents noncombat damage to a planeswalker it protects")
    void preventsNoncombatDamageToPlaneswalker() {
        Permanent wanderer = addReadyWanderer(player1, 3);

        castShock(player2, wanderer.getId());

        assertThat(wanderer.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not prevent combat damage to permanents it protects")
    void doesNotPreventCombatDamage() {
        addReadyWanderer(player1, 3);
        Permanent blocker = addReadyCreature(player1, new GrizzlyBears());
        Permanent attacker = addReadyCreature(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("-2 exiles a creature with power 4 or greater")
    void minusTwoExilesLargeCreature() {
        Permanent wanderer = addReadyWanderer(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wanderer.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() == target.getCard());
    }

    @Test
    @DisplayName("-2 cannot target a creature with power less than 4")
    void minusTwoRejectsSmallCreature() {
        addReadyWanderer(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWanderer(Player player, int loyalty) {
        Permanent wanderer = new Permanent(new TheWanderer());
        wanderer.setCounterCount(CounterType.LOYALTY, loyalty);
        wanderer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wanderer);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return wanderer;
    }

    private Permanent addReadyCreature(Player player, GrizzlyBears card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void castShock(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }
}
