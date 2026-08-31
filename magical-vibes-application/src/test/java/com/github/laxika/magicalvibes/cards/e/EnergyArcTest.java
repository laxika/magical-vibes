package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AshnodsCylix;
import com.github.laxika.magicalvibes.cards.g.GuerrillaTactics;
import com.github.laxika.magicalvibes.cards.g.GargantuanGorilla;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnergyArc.class, ElvishRanger.class, GargantuanGorilla.class, AshnodsCylix.class, GuerrillaTactics.class})
class EnergyArcTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps every target creature")
    void untapsAllTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ElvishRanger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ElvishRanger());
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
        Permanent blocker = addBlocker(player2, 1, 1, 0);

        castEnergyArc(List.of(attacker.getId()));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(blocker.getId()));
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Applies both combat-prevention effects to every chosen creature")
    void preventsCombatDamageForEveryTarget() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        Permanent blocker = addBlocker(player2, 1, 1, 0);

        castEnergyArc(List.of(attacker.getId(), blocker.getId()));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Only combat damage is prevented; noncombat damage still lands")
    void onlyCombatDamagePrevented() {
        Card creatureCard = new ElvishRanger();
        creatureCard.setToughness(3);
        Permanent creature = addCreatureReady(player1, creatureCard);

        castEnergyArc(List.of(creature.getId()));

        harness.setHand(player2, List.of(new GuerrillaTactics()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat-only prevention does not stop noncombat damage dealt by a targeted creature")
    void onlyCombatDamagePreventedForSource() {
        Permanent gorilla = addCreatureReady(player1, new GargantuanGorilla());
        Card targetCard = new ElvishRanger();
        targetCard.setPower(1);
        targetCard.setToughness(8);
        Permanent target = addCreatureReady(player2, targetCard);

        castEnergyArc(List.of(gorilla.getId()));
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(7);
        assertThat(gorilla.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void castsWithNoTargets() {
        castEnergyArc(List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AshnodsCylix());
        harness.setHand(player1, List.of(new EnergyArc()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEnergyArc(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new EnergyArc()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card creature = new ElvishRanger();
        creature.setPower(power);
        creature.setToughness(toughness);
        Permanent perm = addCreatureReady(owner, creature);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent addBlocker(Player owner, int power, int toughness, int blockedAttackerIndex) {
        Card creature = new ElvishRanger();
        creature.setPower(power);
        creature.setToughness(toughness);
        Permanent perm = addCreatureReady(owner, creature);
        perm.setBlocking(true);
        perm.addBlockingTarget(blockedAttackerIndex);
        return perm;
    }
}
