package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrixInterlacer.class, Forest.class, Ornithopter.class})
class DrixInterlacerTest extends BaseCardTest {

    @Test
    void anotherArtifactYouControlAddsIntensity() {
        Permanent drix = harness.addToBattlefieldAndReturn(player1, new DrixInterlacer());
        harness.setHand(player1, List.of(new Ornithopter(), new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(drix.getCounterCount(CounterType.INTENSITY)).isEqualTo(2);
    }

    @Test
    void sacrificeDrawsHalfIntensityRoundedDown() {
        Permanent drix = harness.addToBattlefieldAndReturn(player1, new DrixInterlacer());
        drix.setCounterCount(CounterType.INTENSITY, 3);
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drix Interlacer");
        harness.assertInGraveyard(player1, "Drix Interlacer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1)
                .contains(drawn);
    }

    @Test
    void abilityIsSorcerySpeedOnly() {
        harness.addToBattlefieldAndReturn(player1, new DrixInterlacer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
