package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Drogskol Shieldmate")
class DrogskolShieldmateTest extends BaseCardTest {

    private void castShieldmate() {
        harness.setHand(player1, new ArrayList<>(List.of(new DrogskolShieldmate())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("ETB gives other creatures you control +0/+1 until end of turn")
    void etbBoostsOtherOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castShieldmate();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB boost

        Permanent shieldmate = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drogskol Shieldmate"))
                .findFirst().orElseThrow();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(shieldmate.getPowerModifier()).isEqualTo(0);
        assertThat(shieldmate.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost creatures an opponent controls")
    void doesNotBoostOpponents() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShieldmate();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentBears.getPowerModifier()).isEqualTo(0);
        assertThat(opponentBears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castShieldmate();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }
}
