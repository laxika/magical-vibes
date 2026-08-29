package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarCadenceTest extends BaseCardTest {

    @Test
    @DisplayName("Charges the chosen X for each blocking creature")
    void chargesXForEachBlocker() {
        harness.addToBattlefield(player1, new WarCadence());
        Permanent firstAttacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent secondAttacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent firstBlocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent secondBlocker = addReadyCreature(player2, new GrizzlyBears());
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
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent firstBlocker = addReadyCreature(player2, new GrizzlyBears());
        Permanent secondBlocker = addReadyCreature(player2, new GrizzlyBears());
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
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
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

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int blockerIndex(Permanent blocker) {
        return gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
    }

    private int attackerIndex(Permanent attacker) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
    }
}
