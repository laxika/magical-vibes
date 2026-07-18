package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConservatorTest extends BaseCardTest {

    @Test
    @DisplayName("Ability prevents the next 2 combat damage dealt to controller")
    void preventsTwoDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Conservator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Opponent attacks the shielded controller with a 2/2.
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 2 damage fully prevented → life unchanged.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the next 2 damage is prevented; excess still gets through")
    void preventsOnlyTwoOfLargerHit() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Conservator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Opponent attacks with a 5/5 → 2 prevented, 3 gets through.
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setPowerModifier(3);
        attacker.setToughnessModifier(3);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot activate without paying the {3} cost")
    void requiresManaCost() {
        harness.addToBattlefield(player1, new Conservator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
