package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrudgeReavers.class, AshcoatBear.class})
class DrudgeReaversTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Drudge Reavers to be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        harness.setHand(player1, List.of(new DrudgeReavers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Reavers");
    }

    @Test
    @DisplayName("{B} grants Drudge Reavers a regeneration shield")
    void activationGrantsRegenerationShield() {
        Permanent reavers = addCreatureReady(player1, new DrudgeReavers());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(reavers.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A regeneration shield saves Drudge Reavers from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent reavers = addCreatureReady(player1, new DrudgeReavers());
        reavers.setRegenerationShield(1);
        reavers.setBlocking(true);
        reavers.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AshcoatBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Reavers");
        assertThat(reavers.isTapped()).isTrue();
        assertThat(reavers.getRegenerationShield()).isZero();
    }

}
