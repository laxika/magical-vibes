package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MischievousPoltergeistTest extends BaseCardTest {

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

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mischievous Poltergeist");
        harness.assertInGraveyard(player1, "Mischievous Poltergeist");
    }
}
