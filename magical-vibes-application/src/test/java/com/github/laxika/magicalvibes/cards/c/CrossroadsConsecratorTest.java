package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrossroadsConsecratorTest extends BaseCardTest {

    @Test
    @DisplayName("Gives an attacking Human +1/+1 until end of turn")
    void boostsAttackingHuman() {
        Permanent attacker = setupAttackingTarget(new EliteVanguard());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = setupAttackingTarget(new EliteVanguard());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects a non-Human attacking target")
    void rejectsNonHumanAttacker() {
        Permanent nonHumanAttacker = setupAttackingTarget(new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonHumanAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a nonattacking Human target")
    void rejectsNonattackingHuman() {
        addCreatureReady(player1, new CrossroadsConsecrator());
        Permanent nonAttackingHuman = addCreatureReady(player1, new EliteVanguard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonAttackingHuman.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent setupAttackingTarget(Card targetCard) {
        addCreatureReady(player1, new CrossroadsConsecrator());
        Permanent attacker = addCreatureReady(player1, targetCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        return attacker;
    }
}
