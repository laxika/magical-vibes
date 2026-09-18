package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NeedleshotGourna;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiptideMangler.class, NeedleshotGourna.class, Forest.class})
class RiptideManglerTest extends BaseCardTest {

    @Test
    @DisplayName("Sets base power to the target creature's power indefinitely")
    void setsBasePowerToTargetPowerIndefinitely() {
        Permanent mangler = addCreatureReady(player1, new RiptideMangler());
        Permanent target = addCreatureReady(player2, new NeedleshotGourna());
        target.setPowerModifier(2);
        int originalToughness = gqs.getEffectiveToughness(gd, mangler);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(mangler.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mangler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mangler)).isEqualTo(originalToughness);

        target.setPowerModifier(0);
        assertThat(gqs.getEffectivePower(gd, mangler)).isEqualTo(5);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mangler)).isEqualTo(5);
    }

    @Test
    @DisplayName("Uses the target creature's power when the ability resolves")
    void readsTargetPowerAtResolution() {
        Permanent mangler = addCreatureReady(player1, new RiptideMangler());
        Permanent target = addCreatureReady(player2, new NeedleshotGourna());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setPowerModifier(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mangler)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target any creature but not a land")
    void requiresCreatureTarget() {
        addCreatureReady(player1, new RiptideMangler());
        Permanent land = addCreatureReady(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
