package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarrowBats.class, Vorstclaw.class})
class MarrowBatsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 4 life grants a regeneration shield")
    void payLifeGrantsRegenerationShield() {
        Permanent bats = addCreatureReady(player1, new MarrowBats());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 16);
        assertThat(bats.getRegenerationShield()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(bats.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with less than 4 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new MarrowBats());
        harness.setLife(player1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Regeneration shield saves Marrow Bats from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent bats = addCreatureReady(player1, new MarrowBats());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        bats.setBlocking(true);
        bats.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new Vorstclaw());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Marrow Bats");
        assertThat(bats.isTapped()).isTrue();
        assertThat(bats.getRegenerationShield()).isEqualTo(0);
        assertThat(bats.getMarkedDamage()).isZero();
        assertThat(bats.isBlocking()).isFalse();
        assertThat(bats.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Marrow Bats dies to lethal combat damage without a shield")
    void diesWithoutShield() {
        Permanent bats = addCreatureReady(player1, new MarrowBats());
        bats.setBlocking(true);
        bats.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new Vorstclaw());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Marrow Bats");
        harness.assertInGraveyard(player1, "Marrow Bats");
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent bats = harness.addToBattlefieldAndReturn(player1, new MarrowBats());
        bats.setSummoningSick(true);
        bats.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(bats.getRegenerationShield()).isEqualTo(1);
        assertThat(bats.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple activations pay separately and a destruction uses only one shield")
    void multipleActivationsProvideSeparateShields() {
        Permanent bats = addCreatureReady(player1, new MarrowBats());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        assertThat(bats.getRegenerationShield()).isEqualTo(2);
        assertThat(bats.isTapped()).isFalse();

        bats.setBlocking(true);
        bats.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new Vorstclaw());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Marrow Bats");
        assertThat(bats.getRegenerationShield()).isEqualTo(1);
        assertThat(bats.getMarkedDamage()).isZero();
        assertThat(bats.isTapped()).isTrue();
    }
}
