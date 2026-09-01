package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RemnantElemental.class, Forest.class})
class RemnantElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Remnant Elemental +2/+0 until end of turn")
    void landfallBoostsUntilEndOfTurn() {
        Permanent remnant = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        assertThat(remnant.getEffectivePower()).isZero();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(remnant.getEffectivePower()).isEqualTo(2);
        assertThat(remnant.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(remnant.getEffectivePower()).isZero();
        assertThat(remnant.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent remnant = harness.addToBattlefieldAndReturn(player1, new RemnantElemental());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(remnant.getEffectivePower()).isZero();
    }
}
