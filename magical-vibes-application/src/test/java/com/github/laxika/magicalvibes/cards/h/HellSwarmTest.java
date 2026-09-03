package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed({HellSwarm.class, GrizzlyBears.class})
class HellSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -1/-0 to every creature on both battlefields")
    void debuffsAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new HellSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent own = findPermanent(player1, "Grizzly Bears");
        Permanent theirs = findPermanent(player2, "Grizzly Bears");
        assertThat(own.getEffectivePower()).isEqualTo(1);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(1);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HellSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }
}
