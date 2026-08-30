package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingMusicians.class, GrizzlyBears.class})
class WanderingMusiciansTest extends BaseCardTest {

    @Test
    void attackingBoostsAllCreaturesYouControl() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(musicians.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);
        assertThat(opposingCreature.getPowerModifier()).isZero();
        assertThat(musicians.getToughnessModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();
    }

    @Test
    void attackBonusExpiresAtEndOfTurn() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(musicians.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(musicians.getPowerModifier()).isZero();
        assertThat(otherCreature.getPowerModifier()).isZero();
    }
}
