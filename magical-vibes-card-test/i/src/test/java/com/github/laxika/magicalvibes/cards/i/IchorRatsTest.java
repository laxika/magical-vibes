package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.cards.w.WallOfTanglecord;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IchorRats.class, GraspOfDarkness.class, WallOfTanglecord.class})
class IchorRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ichor Rats gives each player a poison counter")
    void etbGivesEachPlayerPoisonCounter() {
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int p1PoisonBefore = gd.playerPoisonCounters.getOrDefault(player1.getId(), 0);
        int p2PoisonBefore = gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);

        harness.castCreature(player1, 0);
        // Resolve creature spell (ETB triggers go on stack)
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0))
                .isEqualTo(p1PoisonBefore + 1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0))
                .isEqualTo(p2PoisonBefore + 1);
    }

    @Test
    @DisplayName("Ichor Rats enters the battlefield when cast")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ichor Rats");
    }

    @Test
    @DisplayName("Multiple Ichor Rats ETBs accumulate poison counters")
    void multipleEtbsAccumulatePoison() {
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        // Cast a second Ichor Rats
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enter trigger resolves after Ichor Rats dies without dealing damage")
    void enterTriggerSurvivesSourceRemoval() {
        Permanent rats = harness.enterBattlefieldAndReturn(player1, new IchorRats());
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();

        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, rats.getId());
        harness.assertInGraveyard(player1, "Ichor Rats");
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unblocked infect damage gives two poison counters without life loss")
    void unblockedDamageGivesPoison() {
        Permanent rats = harness.addToBattlefieldAndReturn(player1, new IchorRats());
        rats.setSummoningSick(false);
        rats.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.resolveCombatDamage();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blocked infect damage puts counters on the blocker instead of marked damage")
    void blockedDamageGivesMinusCounters() {
        Permanent rats = harness.addToBattlefieldAndReturn(player1, new IchorRats());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfTanglecord());
        rats.setSummoningSick(false);
        rats.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.resolveCombatDamage();

        assertThat(wall.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(wall.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Ichor Rats");
        harness.assertOnBattlefield(player2, "Wall of Tanglecord");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertLife(player2, 20);
    }
}
