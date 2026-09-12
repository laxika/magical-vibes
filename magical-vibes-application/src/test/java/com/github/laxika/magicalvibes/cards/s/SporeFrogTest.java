package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SporeFrog.class, PygmyRazorback.class, SearingWind.class})
class SporeFrogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Spore Frog prevents all combat damage this turn")
    void sacrificePreventsCombatDamage() {
        harness.addToBattlefield(player1, new SporeFrog());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spore Frog");
        harness.assertInGraveyard(player1, "Spore Frog");
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage after the sacrifice")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SporeFrog());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        var attacker = addCreatureReady(player2, new PygmyRazorback());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The combat prevention does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SporeFrog());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SearingWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("The combat prevention ends at the end of the turn")
    void combatPreventionEndsAtEndOfTurn() {
        harness.addToBattlefield(player1, new SporeFrog());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isFalse();
    }
}
