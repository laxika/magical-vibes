package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElHajjJTest extends BaseCardTest {

    private Permanent addAttacker(ElHajjJ card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage
        harness.passBothPriorities(); // resolve the triggered ability
    }

    @Test
    @DisplayName("Combat damage to a player gains that much life")
    void combatDamageToPlayerGainsLife() {
        addAttacker(new ElHajjJ());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19); // took 1 combat damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21); // gained 1 life
    }

    @Test
    @DisplayName("Life gained equals the total damage dealt, not a fixed amount")
    void lifeGainedEqualsDamageDealt() {
        ElHajjJ hajjaj = new ElHajjJ();
        hajjaj.setPower(3);
        addAttacker(hajjaj);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17); // took 3 combat damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23); // gained 3 life
    }

    @Test
    @DisplayName("Still gains life from damage dealt to a blocker even when it dies in combat")
    void gainsLifeFromCreatureDamageWhenItDies() {
        addAttacker(new ElHajjJ());
        harness.setLife(player1, 20);

        // Grizzly Bears (2/2) blocks the 1/1 — El-Hajjâj deals 1 to it and dies to the 2 back.
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombatAndTrigger();

        // El-Hajjâj traded away but its "deals damage" trigger still resolved.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("El-Hajjâj"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21); // gained 1 life
    }

    @Test
    @DisplayName("Does not gain life when it deals no combat damage")
    void noLifeWhenBlockedByLargerAndDealsNoDamage() {
        ElHajjJ hajjaj = new ElHajjJ();
        hajjaj.setPower(0); // deals no damage
        addAttacker(hajjaj);
        harness.setLife(player1, 20);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20); // no life gained
    }
}
