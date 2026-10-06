package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
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

@CardUsed({ScreechingHarpy.class, LowlandGiant.class})
class ScreechingHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Screeching Harpy")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent harpy = addCreatureReady(player1, new ScreechingHarpy());
        Permanent blocker = addCreatureReady(player2, new LowlandGiant());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(harpy)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying {1}{B} grants a regeneration shield")
    void payGrantsRegenerationShield() {
        Permanent harpy = addCreatureReady(player1, new ScreechingHarpy());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harpy.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateRegenerationWithoutEnoughMana() {
        addCreatureReady(player1, new ScreechingHarpy());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Screeching Harpy from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent harpy = addCreatureReady(player1, new ScreechingHarpy());
        harpy.setRegenerationShield(1);
        harpy.setBlocking(true);
        harpy.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new LowlandGiant());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Screeching Harpy");
        assertThat(harpy.isTapped()).isTrue();
        assertThat(harpy.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Screeching Harpy dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent harpy = addCreatureReady(player1, new ScreechingHarpy());
        harpy.setBlocking(true);
        harpy.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new LowlandGiant());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Screeching Harpy");
        harness.assertInGraveyard(player1, "Screeching Harpy");
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void regenerationCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent harpy = addCreatureReady(player1, new ScreechingHarpy());
        harpy.setSummoningSick(true);
        harpy.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harpy.getRegenerationShield()).isEqualTo(1);
        assertThat(harpy.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Screeching Harpy");
    }

    @Test
    @DisplayName("Each activation grants a separate shield without tapping or healing the creature")
    void repeatedActivationsCreateSeparateShields() {
        Permanent harpy = addCreatureReady(player1, new ScreechingHarpy());
        harpy.setMarkedDamage(1);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harpy.getRegenerationShield()).isEqualTo(2);
        assertThat(harpy.isTapped()).isFalse();
        assertThat(harpy.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("An activated shield replaces lethal damage, clears damage, and removes the Harpy from combat")
    void activatedShieldReplacesLethalCombatDamage() {
        Permanent harpy = addCreatureReady(player1, new ScreechingHarpy());
        Permanent attacker = addCreatureReady(player2, new LowlandGiant());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harpy.setBlocking(true);
        harpy.addBlockingTarget(0);
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Screeching Harpy");
        harness.assertNotInGraveyard(player1, "Screeching Harpy");
        assertThat(harpy.isTapped()).isTrue();
        assertThat(harpy.getRegenerationShield()).isZero();
        assertThat(harpy.getMarkedDamage()).isZero();
        assertThat(harpy.isBlocking()).isFalse();
        assertThat(harpy.getBlockingTargets()).isEmpty();
    }
}
