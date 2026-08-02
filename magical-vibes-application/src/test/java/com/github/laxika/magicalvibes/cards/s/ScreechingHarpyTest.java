package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScreechingHarpyTest extends BaseCardTest {

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
    @DisplayName("Regeneration shield saves Screeching Harpy from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent harpy = addCreatureReady(player1, new ScreechingHarpy());
        harpy.setRegenerationShield(1);
        harpy.setBlocking(true);
        harpy.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Screeching Harpy");
        harness.assertInGraveyard(player1, "Screeching Harpy");
    }
}
