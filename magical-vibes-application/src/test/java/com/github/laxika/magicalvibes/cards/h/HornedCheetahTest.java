package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Backlash;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornedCheetah.class, HoodedKavu.class, Backlash.class})
class HornedCheetahTest extends BaseCardTest {

    private Permanent addAttacker(HornedCheetah card) {
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
        addAttacker(new HornedCheetah());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gained equals the damage dealt")
    void lifeGainedEqualsDamageDealt() {
        HornedCheetah cheetah = new HornedCheetah();
        cheetah.setPower(3);
        addAttacker(cheetah);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Still gains life from damage dealt to a blocker when it dies in combat")
    void gainsLifeFromCreatureDamageWhenItDies() {
        addAttacker(new HornedCheetah());
        harness.setLife(player1, 20);

        Permanent blocker = addCreatureReady(player2, new HoodedKavu());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        harness.assertInGraveyard(player1, "Horned Cheetah");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not gain life when it deals no damage")
    void noLifeWhenDealsNoDamage() {
        HornedCheetah cheetah = new HornedCheetah();
        cheetah.setPower(0);
        addAttacker(cheetah);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Noncombat damage to its controller gains that much life")
    void noncombatDamageToControllerGainsLife() {
        Permanent cheetah = addCreatureReady(player2, new HornedCheetah());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Backlash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, cheetah.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
