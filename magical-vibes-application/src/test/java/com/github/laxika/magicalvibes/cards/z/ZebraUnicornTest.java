package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.Delirium;
import com.github.laxika.magicalvibes.cards.j.JungleWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZebraUnicorn.class, JungleWurm.class})
class ZebraUnicornTest extends BaseCardTest {

    private Permanent addAttacker(ZebraUnicorn card) {
        Permanent perm = addCreatureReady(player1, card);
        perm.setAttacking(true);
        return perm;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
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
        unicorn.setPower(1); // dies to the larger blocker but still deals its damage
        addAttacker(unicorn);
        harness.setLife(player1, 20);

        Permanent blocker = addCreatureReady(player2, new JungleWurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

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

    @Test
    @CardUsed(Delirium.class)
    @DisplayName("Noncombat damage also gains that much life")
    void noncombatDamageAlsoGainsLife() {
        Permanent unicorn = addCreatureReady(player2, new ZebraUnicorn());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Delirium()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, unicorn.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
