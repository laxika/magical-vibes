package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantTurtle;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrineHag.class, GiantTurtle.class, ProdigalSorcerer.class, TormodsCrypt.class})
class BrineHagTest extends BaseCardTest {

    @Test
    @DisplayName("Sets every creature that dealt damage to it this turn to base 0/2")
    void setsDamagingCreaturesToBaseZeroTwo() {
        Permanent unaffectedCreature = addCreatureReady(player1, new BrineHag());
        Permanent damagingCreature = killBrineHagAfterCombatDamage();

        assertThat(gqs.getEffectivePower(gd, damagingCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, damagingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, unaffectedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unaffectedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The base-stat change lasts indefinitely")
    void lastsIndefinitely() {
        Permanent damagingCreature = killBrineHagAfterCombatDamage();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, damagingCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, damagingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Changes only base stats, preserving counters on a surviving damage source")
    void preservesCountersOnDamageSource() {
        Permanent hag = addCreatureReady(player1, new BrineHag());
        Permanent turtle = addCreatureReady(player2, new GiantTurtle());
        turtle.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        turtle.setAttacking(true);
        hag.setBlocking(true);
        hag.addBlockingTargetId(turtle.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertInGraveyard(player1, "Brine Hag");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(3);
        assertThat(turtle.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Affects all noncombat damage sources regardless of their controllers")
    void affectsAllNoncombatDamageSources() {
        Permanent hag = addCreatureReady(player1, new BrineHag());
        Permanent friendlySource = addCreatureReady(player1, new ProdigalSorcerer());
        Permanent opposingSource = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent unaffected = addCreatureReady(player2, new ProdigalSorcerer());

        harness.activateAbility(player1, 1, null, hag.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, hag.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Brine Hag");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, friendlySource)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, friendlySource)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingSource)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opposingSource)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, unaffected)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, unaffected)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiling Brine Hag in response does not stop its death trigger")
    void resolvesAfterHagLeavesGraveyard() {
        Permanent hag = addCreatureReady(player1, new BrineHag());
        Permanent turtle = addCreatureReady(player2, new GiantTurtle());
        harness.addToBattlefield(player2, new TormodsCrypt());
        turtle.setAttacking(true);
        hag.setBlocking(true);
        hag.addBlockingTargetId(turtle.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.assertInGraveyard(player1, "Brine Hag");
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player2, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player1, "Brine Hag");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(hag.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, turtle)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(2);
    }

    private Permanent killBrineHagAfterCombatDamage() {
        BrineHag hagCard = new BrineHag();
        hagCard.setPower(0);
        hagCard.setToughness(1);
        Permanent hag = addCreatureReady(player1, hagCard);

        Permanent damagingCreature = addCreatureReady(player2, new BrineHag());
        damagingCreature.setAttacking(true);
        hag.setBlocking(true);
        hag.addBlockingTarget(0);

        resolveCombat(player2);
        harness.passBothPriorities();
        return damagingCreature;
    }
}
