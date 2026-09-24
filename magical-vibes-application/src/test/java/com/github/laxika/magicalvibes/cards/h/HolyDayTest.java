package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HolyDay.class, GrizzlyBears.class, Shock.class})
class HolyDayTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage while Holy Day is in effect")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setHand(player1, List.of(new HolyDay(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Combat damage prevention ends at end of turn")
    void combatDamagePreventionEndsAtEndOfTurn() {
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isFalse();
    }
}
