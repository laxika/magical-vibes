package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RiverBoaTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {G} grants a regeneration shield")
    void payGreenGrantsRegenerationShield() {
        Permanent boa = addCreatureReady(player1, new RiverBoa());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(boa.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves River Boa from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent boa = addCreatureReady(player1, new RiverBoa());
        boa.setRegenerationShield(1);
        boa.setBlocking(true);
        boa.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "River Boa");
        assertThat(boa.isTapped()).isTrue();
        assertThat(boa.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("River Boa dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent boa = addCreatureReady(player1, new RiverBoa());
        boa.setBlocking(true);
        boa.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "River Boa");
        harness.assertInGraveyard(player1, "River Boa");
    }
}
