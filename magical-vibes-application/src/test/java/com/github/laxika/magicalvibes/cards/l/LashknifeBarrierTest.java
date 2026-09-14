package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.m.MagmaBurst;
import com.github.laxika.magicalvibes.cards.s.SeaSnidd;
import com.github.laxika.magicalvibes.cards.s.Singe;
import com.github.laxika.magicalvibes.cards.s.SlingshotGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LashknifeBarrier.class, AlphaKavu.class, MagmaBurst.class, SeaSnidd.class,
        Singe.class, SlingshotGoblin.class})
class LashknifeBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters the battlefield")
    void drawsCardOnEnter() {
        LashknifeBarrier drawn = new LashknifeBarrier();
        harness.setHand(player1, List.of(new LashknifeBarrier()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Reduces damage to creatures you control from a spell")
    void reducesSpellDamageToControlledCreature() {
        addBarrier(player1);
        Permanent seaSnidd = addCreatureReady(player1, new SeaSnidd());

        harness.setHand(player2, List.of(new MagmaBurst()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, seaSnidd.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(seaSnidd);
        assertThat(seaSnidd.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Reduces damage to creatures you control from an ability")
    void reducesAbilityDamageToControlledCreature() {
        addBarrier(player1);
        Permanent seaSnidd = addCreatureReady(player1, new SeaSnidd());
        Permanent goblin = addCreatureReady(player2, new SlingshotGoblin());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(goblin),
                null, seaSnidd.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(seaSnidd);
        assertThat(seaSnidd.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Reduces combat damage to creatures you control")
    void reducesCombatDamageToControlledCreature() {
        addBarrier(player1);
        Permanent blocker = addCreatureReady(player1, new AlphaKavu());
        Permanent attacker = addCreatureReady(player2, new AlphaKavu());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not reduce damage dealt to its controller")
    void doesNotReduceDamageToController() {
        addBarrier(player1);
        harness.setHand(player2, List.of(new MagmaBurst()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Does not reduce damage to an opponent's creature")
    void doesNotReduceDamageToOpponentsCreature() {
        addBarrier(player1);
        Permanent kavu = addCreatureReady(player2, new AlphaKavu());
        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, kavu.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kavu);
        assertThat(kavu.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not reduce damage below zero")
    void doesNotReduceDamageBelowZero() {
        addBarrier(player1);
        Permanent kavu = addCreatureReady(player1, new AlphaKavu());
        harness.setHand(player2, List.of(new Singe()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, kavu.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kavu);
        assertThat(kavu.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Stacks with multiple Lashknife Barriers")
    void stacksWithMultipleBarriers() {
        addBarrier(player1);
        addBarrier(player1);
        Permanent seaSnidd = addCreatureReady(player1, new SeaSnidd());

        harness.setHand(player2, List.of(new MagmaBurst()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, seaSnidd.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(seaSnidd);
        assertThat(seaSnidd.getMarkedDamage()).isEqualTo(1);
    }

    private void addBarrier(Player player) {
        harness.addToBattlefield(player, new LashknifeBarrier());
    }
}
