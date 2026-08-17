package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyGargoyle() {
        Permanent gargoyle = new Permanent(new ThrabenGargoyle());
        gargoyle.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gargoyle);
        return gargoyle;
    }
}
