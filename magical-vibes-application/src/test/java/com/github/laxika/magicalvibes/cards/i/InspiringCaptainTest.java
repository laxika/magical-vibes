package com.github.laxika.magicalvibes.cards.i;

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

@DisplayName("Inspiring Captain")
class InspiringCaptainTest extends BaseCardTest {

    private void castCaptain() {
        harness.setHand(player1, new ArrayList<>(List.of(new InspiringCaptain())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("ETB gives creatures you control +1/+1 until end of turn")
    void etbBoostsOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCaptain();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB boost

        Permanent captain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Inspiring Captain"))
                .findFirst().orElseThrow();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost creatures an opponent controls")
    void doesNotBoostOpponents() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCaptain();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentBears.getPowerModifier()).isEqualTo(0);
        assertThat(opponentBears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCaptain();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }
}
