package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimeboundDead.class})
class RimeboundDeadTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped, summoning-sick Rimebound Dead can regenerate with colored snow mana")
    void tappedSummoningSickCreatureCanActivateWithColoredSnowMana() {
        Permanent dead = harness.addToBattlefieldAndReturn(player1, new RimeboundDead());
        dead.setSummoningSick(true);
        dead.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(dead.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Repeated activations create separate shields only when they resolve")
    void repeatedActivationsCreateShieldsOnResolution() {
        Permanent dead = addCreatureReady(player1, new RimeboundDead());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(dead.getRegenerationShield()).isZero();
        assertThat(dead.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();

        harness.passBothPriorities();
        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(dead.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(dead.getRegenerationShield()).isEqualTo(2);
        assertThat(dead.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Snow mana grants Rimebound Dead a regeneration shield")
    void snowManaGrantsRegenerationShield() {
        Permanent dead = addCreatureReady(player1, new RimeboundDead());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Regeneration clears lethal damage and combat status while consuming only one shield")
    void regenerationConsumesOneShieldAndRemovesDamageAndCombatStatus() {
        Permanent dead = addCreatureReady(player1, new RimeboundDead());
        Permanent attacker = addCreatureReady(player2, new RimeboundDead());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        dead.setBlocking(true);
        dead.addBlockingTarget(0);
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Rimebound Dead");
        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(dead.getMarkedDamage()).isZero();
        assertThat(dead.isTapped()).isTrue();
        assertThat(dead.isBlocking()).isFalse();
        assertThat(dead.getBlockingTargets()).isEmpty();
        harness.assertInGraveyard(player2, "Rimebound Dead");
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addCreatureReady(player1, new RimeboundDead());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The regeneration shield saves Rimebound Dead from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent dead = addCreatureReady(player1, new RimeboundDead());
        Permanent attacker = addCreatureReady(player2, new RimeboundDead());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        dead.setBlocking(true);
        dead.addBlockingTarget(0);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Rimebound Dead");
        assertThat(dead.isTapped()).isTrue();
        assertThat(dead.getRegenerationShield()).isZero();
    }
}
