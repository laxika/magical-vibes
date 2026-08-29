package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteelgazeGriffin.class, GrizzlyBears.class})
class SteelgazeGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn gives Steelgaze Griffin +2/+0 only once")
    void boostsOnSecondDrawOnlyOnce() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SteelgazeGriffin());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawCard();
        assertThat(griffin.getPowerModifier()).isZero();

        drawCard();
        assertThat(gd.stack).hasSize(1);
        drawCard();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(griffin.getPowerModifier()).isEqualTo(2);
        assertThat(griffin.getToughnessModifier()).isZero();
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
