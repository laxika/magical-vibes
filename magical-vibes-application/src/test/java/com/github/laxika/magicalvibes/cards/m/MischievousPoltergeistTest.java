package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MischievousPoltergeist.class, GrizzlyBears.class})
class MischievousPoltergeistTest extends BaseCardTest {
    @Test
    void canStackMultipleRegenerationShields() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        poltergeist.tap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(poltergeist.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying 1 life grants a regeneration shield")
    void payLifeGrantsRegenerationShield() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(poltergeist.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability with no life to pay")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Regeneration shield saves Mischievous Poltergeist from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        poltergeist.setRegenerationShield(1);
        poltergeist.setBlocking(true);
        poltergeist.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Mischievous Poltergeist");
        assertThat(poltergeist.isTapped()).isTrue();
        assertThat(poltergeist.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mischievous Poltergeist dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        poltergeist.setBlocking(true);
        poltergeist.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Mischievous Poltergeist");
        harness.assertInGraveyard(player1, "Mischievous Poltergeist");
    }
}
