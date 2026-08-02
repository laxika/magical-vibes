package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrimSamiteHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability taps Orim and sets a 3-damage prevention shield on resolution")
    void activationSetsShield() {
        Permanent orim = addReadyOrim(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        assertThat(orim.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(harness.getGameData().globalDamagePreventionShield).isEqualTo(3);
    }

    @Test
    @DisplayName("Shield prevents combat damage to a player and keeps the unused remainder")
    void preventsCombatDamageToPlayer() {
        addReadyOrim(player2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.globalDamagePreventionShield).isEqualTo(1);
    }

    @Test
    @DisplayName("Shield saves a creature from lethal combat damage")
    void preventsCombatDamageToCreature() {
        addReadyOrim(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().globalDamagePreventionShield).isEqualTo(1);
    }

    @Test
    @DisplayName("Unused prevention shield wears off at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyOrim(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getGameData().globalDamagePreventionShield).isEqualTo(0);
    }

    private Permanent addReadyOrim(Player player) {
        Permanent orim = new Permanent(new OrimSamiteHealer());
        orim.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(orim);
        return orim;
    }
}
