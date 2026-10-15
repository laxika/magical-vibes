package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarnageWurm.class})
class CarnageWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 3: enters with three +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castWurm();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodthirst 3: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castWurm();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 3 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castWurm();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst checks damage when the creature enters, not when it is cast")
    void damageAfterCastingEnablesBloodthirst() {
        harness.setHand(player1, List.of(new CarnageWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);

        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodthirst gives exactly three counters regardless of the damage amount")
    void bloodthirstCounterCountIsFixed() {
        gd.recordDamageToPlayer(player2.getId(), 10);
        castWurm();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Life loss without damage does not enable bloodthirst")
    void lifeLossDoesNotEnableBloodthirst() {
        harness.setLife(player2, 17);
        castWurm();

        assertThat(findPermanent(player1, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst checks the opponent of the creature's controller")
    void bloodthirstForSecondPlayer() {
        harness.forceActivePlayer(player2);
        gd.recordDamageToPlayer(player1.getId(), 1);
        harness.setHand(player2, List.of(new CarnageWurm()));
        harness.addMana(player2, ManaColor.GREEN, 7);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Carnage Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A bloodthirst-enhanced Wurm tramples over an unenhanced Wurm")
    void trampleDealsExcessDamage() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castWurm();
        Permanent attacker = findPermanent(player1, "Carnage Wurm");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CarnageWurm());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 6,
                player2.getId(), 3
        ));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Carnage Wurm");
        harness.assertOnBattlefield(player1, "Carnage Wurm");
    }
    private void castWurm() {
        harness.setHand(player1, List.of(new CarnageWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
