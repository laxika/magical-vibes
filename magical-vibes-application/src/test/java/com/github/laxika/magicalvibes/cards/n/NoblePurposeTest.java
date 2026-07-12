package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoblePurposeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life when a creature they control deals combat damage to a player")
    void gainsLifeOnCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addNoblePurpose(player1);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Player2 takes 2 combat damage; Player1 gains that much life.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gain scales with the damage dealt")
    void lifeGainScalesWithDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addNoblePurpose(player1);
        GrizzlyBears big = new GrizzlyBears();
        big.setPower(5);
        big.setToughness(5);
        Permanent attacker = addReadyCreature(player1, big);
        attacker.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Fires on combat damage dealt to a blocking creature")
    void gainsLifeOnCombatDamageToBlocker() {
        harness.setLife(player1, 20);

        addNoblePurpose(player1);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Attacker dealt 2 to the blocker → controller gains 2 life.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Only creatures the enchantment's controller controls trigger the life gain")
    void onlyOwnCreaturesTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 owns Noble Purpose but does not attack.
        addNoblePurpose(player1);

        // Player2's creature deals combat damage to player1 — should NOT gain player1 life.
        harness.forceActivePlayer(player2);
        Permanent enemy = addReadyCreature(player2, new GrizzlyBears());
        enemy.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Player2 controls no Noble Purpose, so no life gain.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each attacking creature triggers the life gain separately")
    void triggersPerCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addNoblePurpose(player1);
        Permanent a = addReadyCreature(player1, new GrizzlyBears());
        a.setAttacking(true);
        Permanent b = addReadyCreature(player1, new GrizzlyBears());
        b.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Two 2/2s deal 4 total to player2; controller gains 4.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, GrizzlyBears card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addNoblePurpose(Player controller) {
        Permanent enchantment = new Permanent(new NoblePurpose());
        harness.getGameData().playerBattlefields.get(controller.getId()).add(enchantment);
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
