package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProfessionalWrestler;
import com.github.laxika.magicalvibes.cards.r.RoboticsMastery;
import com.github.laxika.magicalvibes.cards.w.WebShooters;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SPDrPilotedByPeni.class, GrizzlyBears.class, ProfessionalWrestler.class,
        RoboticsMastery.class, WebShooters.class})
class SPDrPilotedByPeniTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by putting a +1/+1 counter on a target creature")
    void entersWithTargetCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SPDrPilotedByPeni()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card when a modified creature you control deals combat damage to a player")
    void drawsForModifiedCreatureCombatDamage() {
        addCreatureReady(player1, new SPDrPilotedByPeni());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedAttacker.setAttacking(true);
        harness.setHand(player1, List.of());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw for an unmodified creature dealing combat damage")
    void doesNotDrawForUnmodifiedCreatureCombatDamage() {
        addCreatureReady(player1, new SPDrPilotedByPeni());
        Permanent unmodifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        unmodifiedAttacker.setAttacking(true);
        harness.setHand(player1, List.of());

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void entersCanPutCounterOnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ProfessionalWrestler());
        harness.setHand(player1, List.of(new SPDrPilotedByPeni()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void drawsForItsOwnCombatDamageWithANonPowerCounter() {
        Permanent attacker = addCreatureReady(player1, new SPDrPilotedByPeni());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ProfessionalWrestler()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player2, 16);
    }

    @Test
    void drawsForEachModifiedCreatureRatherThanOncePerCombat() {
        Permanent source = addCreatureReady(player1, new SPDrPilotedByPeni());
        Permanent ally = addCreatureReady(player1, new ProfessionalWrestler());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        ally.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        source.setAttacking(true);
        ally.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ProfessionalWrestler(), new ProfessionalWrestler()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void drawsForCreatureWithOwnAura() {
        Permanent attacker = addCreatureReady(player1, new SPDrPilotedByPeni());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RoboticsMastery());
        aura.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ProfessionalWrestler()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawForCreatureWithOnlyOpponentsAura() {
        Permanent attacker = addCreatureReady(player1, new SPDrPilotedByPeni());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RoboticsMastery());
        aura.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 14);
    }

    @Test
    void drawsForCreatureWithOpponentsEquipment() {
        Permanent attacker = addCreatureReady(player1, new SPDrPilotedByPeni());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new WebShooters());
        equipment.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ProfessionalWrestler()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsInBothDamageStepsForDoubleStrike() {
        Permanent attacker = addCreatureReady(player1, new SPDrPilotedByPeni());
        attacker.setCounterCount(CounterType.DOUBLE_STRIKE, 1);
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ProfessionalWrestler(), new ProfessionalWrestler()));

        resolveCombat();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 12);
    }

    @Test
    void removingModificationAfterDamageDoesNotStopTheDraw() {
        Permanent attacker = addCreatureReady(player1, new SPDrPilotedByPeni());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ProfessionalWrestler()));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        attacker.setCounterCount(CounterType.CHARGE, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawForOpponentsModifiedCreature() {
        addCreatureReady(player1, new SPDrPilotedByPeni());
        Permanent attacker = addCreatureReady(player2, new ProfessionalWrestler());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 15);
    }
}
