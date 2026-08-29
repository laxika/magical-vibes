package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleetingFlightTest extends BaseCardTest {

    @Test
    void putsCounterOnTargetAndGrantsFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFleetingFlight(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    void flyingAndCombatPreventionEndAtEndOfTurnButCounterRemains() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFleetingFlight(target);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gd.creaturesWithCombatDamagePrevented).doesNotContain(target.getId());
    }

    @Test
    void preventsCombatDamageToTargetCreature() {
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        addBlocker(player2, 3, 3, 0);

        castFleetingFlight(attacker);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(attacker.getId()));
    }

    @Test
    void doesNotPreventNoncombatDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFleetingFlight(target);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new FleetingFlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFleetingFlight(Permanent target) {
        harness.setHand(player1, List.of(new FleetingFlight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent permanent = new Permanent(bears);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent permanent = new Permanent(bears);
        permanent.setSummoningSick(false);
        permanent.setBlocking(true);
        permanent.addBlockingTarget(blockedAttackerIndex);
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }
}
