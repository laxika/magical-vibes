package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LifespringDruid.class)
class LifespringDruidTest extends BaseCardTest {

    @Test
    void addsOneManaOfTheChosenColor() {
        Permanent druid = addCreatureReady(player1, new LifespringDruid());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(druid.isTapped()).isTrue();
    }

    @Test
    void cannotChooseColorlessMana() {
        addCreatureReady(player1, new LifespringDruid());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void canProduceEachOfTheFiveColorsWithoutUsingTheStack() {
        for (ManaColor color : ManaColor.COLORS) {
            Permanent druid = addCreatureReady(player1, new LifespringDruid());
            int index = gd.playerBattlefields.get(player1.getId()).indexOf(druid);

            harness.activateAbility(player1, index, null, null);
            assertThat(gd.stack).isEmpty();
            harness.handleListChoice(player1, color.name());

            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
            assertThat(druid.isTapped()).isTrue();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new LifespringDruid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(druid.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new LifespringDruid());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
