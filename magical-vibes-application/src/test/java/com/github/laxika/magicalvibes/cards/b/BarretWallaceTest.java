package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarretWallace.class, GrizzlyBears.class, SwiftfootBoots.class,
        IronGiant.class, BusterSword.class, KarnLiberated.class})
class BarretWallaceTest extends BaseCardTest {

    @Test
    void dealsCombatDamagePlusNoTriggerDamageWithoutEquippedCreatures() {
        Permanent barret = addCreatureReady(player1, new BarretWallace());
        setLifeTotals(20, 20);

        attackWith(barret);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void dealsDamageForEachEquippedCreatureYouControl() {
        Permanent barret = addCreatureReady(player1, new BarretWallace());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.tap();
        attachEquipment(player1, barret);
        attachEquipment(player1, equippedCreature);
        attachEquipment(player1, equippedCreature);
        attachEquipment(player2, opponentCreature);
        setLifeTotals(20, 20);

        attackWith(barret);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void damagesDefendingPlayerWhenAttackingPlaneswalker() {
        Permanent barret = addCreatureReady(player1, new BarretWallace());
        Permanent equippedCreature = addCreatureReady(player1, new IronGiant());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new BusterSword());
        sword.setAttachedTo(equippedCreature.getId());
        Permanent karn = harness.addToBattlefieldAndReturn(player2, new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 6);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, karn.getId()));
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(barret.isAttacking()).isTrue();
    }

    @Test
    void countsEquippedCreaturesAtResolutionEvenIfBarretLeavesBattlefield() {
        Permanent barret = addCreatureReady(player1, new BarretWallace());
        Permanent creature = addCreatureReady(player1, new IronGiant());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            gd.playerBattlefields.get(player1.getId()).remove(barret);
            gd.playerGraveyards.get(player1.getId()).add(barret.getCard());
            Permanent sword = harness.addToBattlefieldAndReturn(player2, new BusterSword());
            sword.setAttachedTo(creature.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotCountEquipmentRemovedBeforeResolution() {
        Permanent barret = addCreatureReady(player1, new BarretWallace());
        Permanent creature = addCreatureReady(player1, new IronGiant());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new BusterSword());
        sword.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            gd.playerBattlefields.get(player1.getId()).remove(sword);
            gd.playerGraveyards.get(player1.getId()).add(sword.getCard());
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(barret.isAttacking()).isTrue();
    }

    private void attackWith(Permanent creature) {
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
    }

    private void attachEquipment(Player player, Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new SwiftfootBoots());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(creature.getId());
    }

    private void setLifeTotals(int player1Life, int player2Life) {
        harness.setLife(player1, player1Life);
        harness.setLife(player2, player2Life);
    }
}
