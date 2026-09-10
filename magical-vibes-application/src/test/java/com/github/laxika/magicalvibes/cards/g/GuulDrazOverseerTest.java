package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuulDrazOverseer.class, GrizzlyBears.class, Forest.class, Swamp.class})
class GuulDrazOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Swamp land gives other creatures +1/+0")
    void nonSwampLandBoostsOtherCreatures() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(overseer.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Swamp gives other creatures +2/+0")
    void swampBoostsOtherCreaturesTwice() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(overseer.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectivePower()).isEqualTo(4);
        assertThat(otherCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Guul Draz Overseer")
    void opponentLandDoesNotTrigger() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new GuulDrazOverseer());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Swamp()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(overseer.getEffectivePower()).isEqualTo(3);
        assertThat(otherCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GuulDrazOverseer());
        harness.setHand(player1, List.of(new Swamp()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(otherCreature.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(otherCreature.getEffectivePower()).isEqualTo(2);
    }
}
