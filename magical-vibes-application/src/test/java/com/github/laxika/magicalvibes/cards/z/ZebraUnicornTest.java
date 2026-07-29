package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ZebraUnicornTest extends BaseCardTest {

    private Permanent addAttacker(ZebraUnicorn card) {
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
        addAttacker(new ZebraUnicorn());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // took 2 combat damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22); // gained 2 life
    }

    @Test
    @DisplayName("Still gains life from damage dealt to a blocker even when it dies in combat")
    void gainsLifeFromCreatureDamageWhenItDies() {
        ZebraUnicorn unicorn = new ZebraUnicorn();
        unicorn.setPower(1); // dies to the 2/2 blocker but still deals its damage
        addAttacker(unicorn);
        harness.setLife(player1, 20);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombatAndTrigger();

        harness.assertInGraveyard(player1, "Zebra Unicorn");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21); // gained 1 life
    }

    @Test
    @DisplayName("Does not gain life when it deals no damage")
    void noLifeWhenDealsNoDamage() {
        ZebraUnicorn unicorn = new ZebraUnicorn();
        unicorn.setPower(0);
        addAttacker(unicorn);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20); // no life gained
    }
}
