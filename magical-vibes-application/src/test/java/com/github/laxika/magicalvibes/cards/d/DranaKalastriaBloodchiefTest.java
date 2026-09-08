package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DranaKalastriaBloodchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Gives Drana +X/+0 and the target creature -0/-X")
    void givesDranaPowerAndTargetCreatureToughnessReduction() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, drana)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The power boost and toughness reduction wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drana)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new DranaKalastriaBloodchief());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
