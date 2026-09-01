package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ParagonOfModernity.class)
class ParagonOfModernityTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when fewer than three colors are spent")
    void boostsWhenFewerThanThreeColorsAreSpent() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        activateAndResolve();

        assertThat(paragon.getPowerModifier()).isEqualTo(1);
        assertThat(paragon.getToughnessModifier()).isEqualTo(1);
        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when exactly three colors are spent")
    void putsCounterWhenExactlyThreeColorsAreSpent() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        activateAndResolve();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(paragon.getPowerModifier()).isZero();
        assertThat(paragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Uses the colors spent by each individual activation")
    void snapshotsColorsPerActivation() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(paragon.getPowerModifier()).isEqualTo(1);
        assertThat(paragon.getToughnessModifier()).isEqualTo(1);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
