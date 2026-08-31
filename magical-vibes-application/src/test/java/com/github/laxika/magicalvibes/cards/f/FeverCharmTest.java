package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArcanisTheOmnipotent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeverCharm.class, ArcanisTheOmnipotent.class, GrizzlyBears.class})
class FeverCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Haste mode grants haste until end of turn")
    void grantsHasteUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castMode(0, target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Pump mode gives the target creature +2/+0")
    void boostsTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castMode(1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wizard mode deals 3 damage to a target Wizard creature")
    void damagesTargetWizard() {
        Permanent target = addCreatureReady(player2, new ArcanisTheOmnipotent());

        castMode(2, target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Wizard mode rejects a non-Wizard creature")
    void wizardModeRejectsNonWizardCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castMode(2, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wizard");
    }

    private void castMode(int modeIndex, Permanent target) {
        harness.setHand(player1, List.of(new FeverCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castModalInstant(player1, 0, modeIndex, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
