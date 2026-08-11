package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnTheTablesTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects combat damage to the target attacking creature")
    void redirectsCombatDamageToTargetAttackingCreature() {
        Permanent attacker = addReadyStats(player2, 2, 2);
        Permanent target = addReadyStats(player2, 6, 10);
        addReadyStats(player1, 2, 2);
        attacker.setAttacking(true);
        target.setAttacking(true);

        castTurnTheTables(target);
        int lifeBefore = gd.getLife(player1.getId());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(attacker.getMarkedDamage()).isEqualTo(0);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not redirect noncombat damage")
    void doesNotRedirectNoncombatDamage() {
        Permanent target = addReadyStats(player2, 3, 3);
        target.setAttacking(true);
        castTurnTheTables(target);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Requires an attacking creature target")
    void requiresAttackingCreatureTarget() {
        Permanent target = addReadyStats(player2, 3, 3);
        harness.setHand(player1, List.of(new TurnTheTables()));
        addWhiteMana(player1, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The redirect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        Permanent target = addReadyStats(player2, 3, 3);
        target.setAttacking(true);
        castTurnTheTables(target);

        assertThat(gd.turnDamageRedirectToCreatureShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.turnDamageRedirectToCreatureShields).isEmpty();
    }

    private void castTurnTheTables(Permanent target) {
        harness.setHand(player1, List.of(new TurnTheTables()));
        addWhiteMana(player1, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addWhiteMana(Player player, int amount) {
        harness.addMana(player, ManaColor.WHITE, amount);
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
