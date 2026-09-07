package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RubblebeltRioters.class, HillGiant.class})
class RubblebeltRiotersTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 based on the greatest power among creatures you control")
    void boostsByGreatestControlledPower() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(rioters.getPowerModifier()).isEqualTo(3);
        assertThat(rioters.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Opponent creatures do not contribute to the boost")
    void ignoresOpponentCreatures() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(rioters.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        var rioters = addCreatureReady(player1, new RubblebeltRioters());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(rioters.getPowerModifier()).isEqualTo(3);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rioters.getPowerModifier()).isZero();
    }
}
