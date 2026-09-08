package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.Backlash;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElHajjJ.class, GrizzlyBears.class})
class ElHajjJTest extends BaseCardTest {

    private Permanent addAttacker(ElHajjJ card) {
        Permanent perm = addCreatureReady(player1, card);
        perm.setAttacking(true);
        return perm;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
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
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        // El-Hajjâj traded away but its "deals damage" trigger still resolved.
        harness.assertInGraveyard(player1, "El-Hajjâj");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21); // gained 1 life
    }

    @Test
    @DisplayName("Does not gain life when it deals no combat damage")
    void noLifeWhenBlockedByLargerAndDealsNoDamage() {
        ElHajjJ hajjaj = new ElHajjJ();
        hajjaj.setPower(0); // deals no damage
        addAttacker(hajjaj);
        harness.setLife(player1, 20);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20); // no life gained
    }

    @Test
    @CardUsed(Backlash.class)
    void noncombatDamageGainsLife() {
        Permanent hajjaj = addCreatureReady(player2, new ElHajjJ());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Backlash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, hajjaj.getId());
        harness.passBothPriorities();

        assertThat(hajjaj.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
