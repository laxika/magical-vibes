package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthOriginYak.class, GrizzlyBears.class})
class EarthOriginYakTest extends BaseCardTest {

    private void castYak() {
        harness.setHand(player1, List.of(new EarthOriginYak()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB gives your creatures +1/+1 until end of turn")
    void etbBoostsOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castYak();

        Permanent yak = findPermanent(player1, "Earth-Origin Yak");

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(yak.getPowerModifier()).isEqualTo(1);
        assertThat(yak.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not boost creatures an opponent controls")
    void doesNotBoostOpponents() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castYak();

        assertThat(opponentBears.getPowerModifier()).isZero();
        assertThat(opponentBears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castYak();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }
}
