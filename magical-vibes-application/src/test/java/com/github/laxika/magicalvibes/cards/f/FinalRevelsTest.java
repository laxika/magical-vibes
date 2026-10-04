package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinalRevels.class, GrizzlyBears.class, HillGiant.class})
class FinalRevelsTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 gives all creatures +2/+0 until end of turn")
    void plusTwoPowerMode() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isEqualTo(2);
        assertThat(mine.getToughnessModifier()).isEqualTo(0);
        assertThat(theirs.getPowerModifier()).isEqualTo(2);
        assertThat(theirs.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void plusTwoWearsOff() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(mine.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mode 1 gives all creatures -0/-2, killing the 2/2 and sparing the 3/3")
    void minusTwoToughnessMode() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(giant.getPowerModifier()).isEqualTo(0);
        assertThat(giant.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The toughness reduction affects both players and wears off at end of turn")
    void minusTwoWearsOffForBothPlayers() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(mine.getToughnessModifier()).isEqualTo(-2);
        assertThat(theirs.getToughnessModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(mine.getToughnessModifier()).isZero();
        assertThat(theirs.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Neither mode affects creatures entering after resolution")
    void laterCreaturesAreUnaffected() {
        harness.setHand(player1, List.of(new FinalRevels(), new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        Permanent afterBoost = harness.enterBattlefieldAndReturn(player1, new HillGiant());
        assertThat(afterBoost.getPowerModifier()).isZero();
        assertThat(afterBoost.getToughnessModifier()).isZero();

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();
        assertThat(afterBoost.getToughnessModifier()).isEqualTo(-2);

        Permanent afterReduction = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.runStateBasedActions();
        assertThat(afterReduction.getPowerModifier()).isZero();
        assertThat(afterReduction.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing an invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new FinalRevels()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }
}
