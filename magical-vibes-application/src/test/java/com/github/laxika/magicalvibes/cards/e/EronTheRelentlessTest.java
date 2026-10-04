package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EronTheRelentless.class)
class EronTheRelentlessTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Eron attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.castFromHand(player1, new EronTheRelentless(), "{3}{R}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new EronTheRelentless());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent eron = findPermanent(player1, "Eron the Relentless");
        assertThat(eron.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Eron from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new EronTheRelentless());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new EronTheRelentless());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Eron the Relentless");
        Permanent eron = findPermanent(player1, "Eron the Relentless");
        assertThat(eron.isTapped()).isTrue();
        assertThat(eron.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Eron dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCreatureReady(player1, new EronTheRelentless());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new EronTheRelentless());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Eron the Relentless");
        harness.assertInGraveyard(player1, "Eron the Relentless");
    }

    @Test
    @DisplayName("Eron can activate its regeneration ability while tapped")
    void canActivateWhenTapped() {
        Permanent eron = addCreatureReady(player1, new EronTheRelentless());
        eron.tap();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eron.getRegenerationShield()).isEqualTo(1);
        assertThat(eron.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creating a shield does not immediately tap Eron or remove its damage")
    void shieldCreationDoesNotRegenerateImmediately() {
        Permanent eron = addCreatureReady(player1, new EronTheRelentless());
        eron.setMarkedDamage(1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(eron.getRegenerationShield()).isZero();
        assertThat(eron.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(eron.getRegenerationShield()).isEqualTo(1);
        assertThat(eron.isTapped()).isFalse();
        assertThat(eron.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations create separate shields and lethal damage consumes only one")
    void repeatedActivationsProtectAgainstSeparateDestructionEvents() {
        Permanent eron = addCreatureReady(player1, new EronTheRelentless());
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eron.getRegenerationShield()).isEqualTo(2);

        eron.setBlocking(true);
        eron.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new EronTheRelentless());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Eron the Relentless");
        harness.assertNotInGraveyard(player1, "Eron the Relentless");
        assertThat(eron.getRegenerationShield()).isEqualTo(1);
        assertThat(eron.getMarkedDamage()).isZero();
        assertThat(eron.isTapped()).isTrue();
        assertThat(eron.isBlocking()).isFalse();
        assertThat(eron.getBlockingTargets()).isEmpty();
    }
}
