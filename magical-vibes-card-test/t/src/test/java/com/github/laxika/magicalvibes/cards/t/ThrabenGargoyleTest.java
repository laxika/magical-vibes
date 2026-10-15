package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrabenGargoyle.class})
class ThrabenGargoyleTest extends BaseCardTest {

    @Test
    void transformsAfterPayingSixGenericMana() {
        Permanent gargoyle = addReadyGargoyle();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gargoyle.isTransformed()).isTrue();
        assertThat(gargoyle.getCard()).isInstanceOf(StonewingAntagonizer.class);
    }

    @Test
    void cannotTransformWithoutSixGenericMana() {
        addReadyGargoyle();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTransformWhileTappedAndSummoningSick() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new ThrabenGargoyle());
        gargoyle.setSummoningSick(true);
        gargoyle.tap();
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gargoyle.isTransformed()).isFalse();
        harness.passBothPriorities();

        assertThat(gargoyle.isTransformed()).isTrue();
        assertThat(gargoyle.isTapped()).isTrue();
        assertThat(gargoyle.isSummoningSick()).isTrue();
    }

    @Test
    void stackedActivationsDoNotTransformBackToFrontFace() {
        Permanent gargoyle = addReadyGargoyle();
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gargoyle.isTransformed()).isTrue();
        harness.passBothPriorities();

        assertThat(gargoyle.isTransformed()).isTrue();
        assertThat(gargoyle.getCard()).isInstanceOf(StonewingAntagonizer.class);
    }
    private Permanent addReadyGargoyle() {
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new ThrabenGargoyle());
        gargoyle.setSummoningSick(false);
        return gargoyle;
    }
}
