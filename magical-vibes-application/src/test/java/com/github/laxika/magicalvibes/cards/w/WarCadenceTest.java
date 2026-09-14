package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.p.PalaceGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarCadence.class, FreshVolunteers.class})
class WarCadenceTest extends BaseCardTest {

    @Test
    @DisplayName("Charges the chosen X for each blocking creature")
    void chargesXForEachBlocker() {
        harness.addToBattlefield(player1, new WarCadence());
        Permanent firstAttacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent secondAttacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent firstBlocker = addCreatureReady(player2, new FreshVolunteers());
        Permanent secondBlocker = addCreatureReady(player2, new FreshVolunteers());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex(firstBlocker), attackerIndex(firstAttacker)),
                new BlockerAssignment(blockerIndex(secondBlocker), attackerIndex(secondAttacker))));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Rejects a block declaration without enough mana for every blocker")
    void rejectsInsufficientManaForEveryBlocker() {
        harness.addToBattlefield(player1, new WarCadence());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent firstBlocker = addCreatureReady(player2, new FreshVolunteers());
        Permanent secondBlocker = addCreatureReady(player2, new FreshVolunteers());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex(firstBlocker), attackerIndex(attacker)),
                new BlockerAssignment(blockerIndex(secondBlocker), attackerIndex(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay block cost");
    }

    @Test
    @DisplayName("The block tax expires at end of turn")
    void blockTaxExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new WarCadence());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        gd.expireEndOfTurnFloatingEffects();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex(blocker), attackerIndex(attacker))));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing X=0 imposes no block tax")
    void zeroXImposesNoTax() {
        harness.addToBattlefield(player1, new WarCadence());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex(blocker), attackerIndex(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed(PalaceGuard.class)
    @DisplayName("Charges only once when one creature blocks multiple attackers")
    void chargesOnceForEachUniqueBlocker() {
        harness.addToBattlefield(player1, new WarCadence());
        Permanent firstAttacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent secondAttacker = addCreatureReady(player1, new FreshVolunteers());
        Permanent blocker = addCreatureReady(player2, new PalaceGuard());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex(blocker), attackerIndex(firstAttacker)),
                new BlockerAssignment(blockerIndex(blocker), attackerIndex(secondAttacker))));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds())
                .containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    private int blockerIndex(Permanent blocker) {
        return gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
    }

    private int attackerIndex(Permanent attacker) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
    }
}
