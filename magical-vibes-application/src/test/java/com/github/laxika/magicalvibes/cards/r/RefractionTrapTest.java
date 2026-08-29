package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefractionTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage to the controller and redirects it to any target")
    void preventsDamageAndRedirectsIt() {
        castForMana(player2.getId());
        addAttacker(player2);

        runCombatDamage();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.damageRedirectShields.getFirst().remainingAmount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents damage to a controlled permanent and redirects it")
    void preventsDamageToControlledPermanent() {
        castForMana(player2.getId());

        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent attacker = addAttacker(player2);

        runCombatDamage();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can pay {W} after an opponent casts a red instant")
    void canUseAlternateCostAfterRedInstant() {
        castRedInstantFromOpponent();

        harness.setHand(player1, List.of(new RefractionTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        preparePlayer1MainPhase();
        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.damageRedirectShields).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost is unavailable without an opponent red instant or sorcery")
    void alternateCostRequiresRedInstantOrSorcery() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        preparePlayer2MainPhase();
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RefractionTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        preparePlayer1MainPhase();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    private void castForMana(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RefractionTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void castRedInstantFromOpponent() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        preparePlayer2MainPhase();
        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player);
        return attacker;
    }

    private void runCombatDamage() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void preparePlayer1MainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void preparePlayer2MainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
