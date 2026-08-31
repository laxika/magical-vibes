package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MabelsMettle.class, GrizzlyBears.class, Mountain.class})
class MabelsMettleTest extends BaseCardTest {

    @Test
    @DisplayName("The required target gets +2/+2")
    void requiredTargetGetsLargerBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The optional other target gets +1/+1")
    void optionalOtherTargetGetsSmallerBoost() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(firstTarget, secondTarget);

        assertThat(gqs.getEffectivePower(gd, firstTarget)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstTarget)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondTarget)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondTarget)).isEqualTo(3);
    }

    @Test
    @DisplayName("The two target groups cannot choose the same creature")
    void targetsMustBeDifferent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mabel's Mettle cannot target a noncreature")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent... targets) {
        prepareSpell();
        harness.castInstant(player1, 0, Arrays.stream(targets).map(Permanent::getId).toList());
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new MabelsMettle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
