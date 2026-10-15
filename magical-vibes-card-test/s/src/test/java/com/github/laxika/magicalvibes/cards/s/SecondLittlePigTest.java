package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecondLittlePig.class})
class SecondLittlePigTest extends BaseCardTest {

    @Test
    void whiteManaTransformsPigWithoutTappingIt() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new SecondLittlePig());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertSpiritTransformation(pig);
        assertThat(pig.isTapped()).isFalse();
    }

    @Test
    void blackManaTransformsTappedPigAndTransformationSurvivesCleanup() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new SecondLittlePig());
        pig.tap();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertSpiritTransformation(pig);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertSpiritTransformation(pig);
    }

    @Test
    void cannotActivateAfterBecomingSpirit() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new SecondLittlePig());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertSpiritTransformation(pig);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void assertSpiritTransformation(Permanent pig) {
        assertThat(gqs.getEffectivePower(gd, pig)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, pig)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, pig, CardSubtype.BOAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, pig, CardSubtype.SPIRIT)).isTrue();
        assertThat(gqs.hasKeyword(gd, pig, Keyword.FLYING)).isTrue();
    }
}
