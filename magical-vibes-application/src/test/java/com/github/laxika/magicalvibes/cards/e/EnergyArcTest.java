package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnergyArcTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps every target creature")
    void untapsAllTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.tap();
        second.tap();

        castEnergyArc(List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Prevents combat damage dealt by and dealt to the targeted creatures")
    void preventsCombatDamageBothWays() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        Permanent blocker = addBlocker(player2, 3, 3, 0);

        castEnergyArc(List.of(attacker.getId(), blocker.getId()));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Only combat damage is prevented; noncombat damage still lands")
    void onlyCombatDamagePrevented() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEnergyArc(List.of(creature.getId()));

        assertThat(gd.creaturesWithCombatDamagePrevented).contains(creature.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(creature.getId());
        assertThat(gd.creaturesWithAllDamagePrevented).doesNotContain(creature.getId());
        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void castsWithNoTargets() {
        harness.setHand(player1, List.of(new EnergyArc()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.creaturesWithCombatDamagePrevented).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.setHand(player1, List.of(new EnergyArc()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEnergyArc(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new EnergyArc()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = new Permanent(bears);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Card bears = new GrizzlyBears();
        bears.setPower(power);
        bears.setToughness(toughness);
        Permanent perm = new Permanent(bears);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(blockedAttackerIndex);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }
}
