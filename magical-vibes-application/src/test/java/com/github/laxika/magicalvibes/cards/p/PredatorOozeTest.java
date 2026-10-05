package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScornedVillager;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatorOoze.class, CruelEdict.class, Deathmark.class, GrizzlyBears.class,
        ScornedVillager.class, TragicSlip.class})
class PredatorOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when it attacks")
    void getsCounterWhenAttacking() {
        Permanent ooze = addCreatureReady(player1, new PredatorOoze());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Predator Ooze"));

        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged dies in combat")
    void getsCounterWhenDamagedCreatureDiesInCombat() {
        Permanent ooze = addCreatureReady(player1, new PredatorOoze());
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        Permanent blocker = addCreatureReady(player2, smallCreature);
        ooze.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ooze.getId()));
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers when a creature it damaged dies later the same turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent ooze = addCreatureReady(player1, new PredatorOoze());
        GrizzlyBears toughCreature = new GrizzlyBears();
        toughCreature.setPower(1);
        toughCreature.setToughness(5);
        Permanent blocker = addCreatureReady(player2, toughCreature);
        ooze.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an undamaged creature dies")
    void noTriggerWhenUndamagedCreatureDies() {
        harness.addToBattlefield(player1, new PredatorOoze());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ooze = findPermanent(player1, "Predator Ooze");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).noneMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Predator Ooze"));
    }

    @Test
    @DisplayName("Blocking earns only the counter for killing the damaged creature")
    void blockingDoesNotTriggerAttackAbility() {
        Permanent attacker = addCreatureReady(player2, new ScornedVillager());
        Permanent ooze = addCreatureReady(player1, new PredatorOoze());
        attacker.setAttacking(true);
        ooze.setBlocking(true);
        ooze.addBlockingTarget(0);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Scorned Villager");
        harness.assertOnBattlefield(player1, "Predator Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature damaged on a previous turn dying does not give a counter")
    void damageHistoryExpiresAtTurnBoundary() {
        Permanent ooze = addCreatureReady(player1, new PredatorOoze());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        ooze.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        harness.assertOnBattlefield(player1, "Predator Ooze");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, blocker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Predator Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Indestructible prevents a destroy spell")
    void survivesDestroySpell() {
        Permanent ooze = addCreatureReady(player2, new PredatorOoze());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, ooze.getId());

        harness.assertOnBattlefield(player2, "Predator Ooze");
        harness.assertNotInGraveyard(player2, "Predator Ooze");
    }

    @Test
    @DisplayName("Indestructible does not prevent dying from zero toughness")
    void diesFromZeroToughness() {
        Permanent ooze = addCreatureReady(player2, new PredatorOoze());
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, ooze.getId());

        harness.assertNotOnBattlefield(player2, "Predator Ooze");
        harness.assertInGraveyard(player2, "Predator Ooze");
    }

    @Test
    @DisplayName("Indestructible does not prevent sacrifice")
    void canBeSacrificed() {
        harness.addToBattlefield(player2, new PredatorOoze());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Predator Ooze");
        harness.assertInGraveyard(player2, "Predator Ooze");
    }
}
