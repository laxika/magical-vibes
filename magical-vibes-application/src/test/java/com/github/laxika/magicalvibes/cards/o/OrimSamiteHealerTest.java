package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrimSamiteHealer.class, GrizzlyBears.class})
class OrimSamiteHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability taps Orim and sets a 3-damage prevention shield on resolution")
    void activationSetsShield() {
        Permanent orim = addReadyOrim(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player1.getId());
        assertThat(orim.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shield prevents combat damage to a player and keeps the unused remainder")
    void preventsCombatDamageToPlayer() {
        addReadyOrim(player2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player2.getId());
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
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shield saves a creature from lethal combat damage")
    void preventsCombatDamageToCreature() {
        addReadyOrim(player1);
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(blocker.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unused prevention shield wears off at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyOrim(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
    }

    private Permanent addReadyOrim(Player player) {
        Permanent orim = new Permanent(new OrimSamiteHealer());
        orim.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(orim);
        return orim;
    }
}
