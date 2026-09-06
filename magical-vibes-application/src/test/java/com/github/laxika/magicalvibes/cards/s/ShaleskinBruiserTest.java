package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EmberBeast;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShaleskinBruiser.class, EmberBeast.class, GrizzlyBears.class})
class ShaleskinBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 for each other attacking Beast")
    void boostsForOtherAttackingBeasts() {
        Permanent bruiser = addCreatureReady(player1, new ShaleskinBruiser());
        Permanent beast = addCreatureReady(player1, new EmberBeast());
        Permanent nonBeast = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bruiser)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonBeast)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bruiser = addCreatureReady(player1, new ShaleskinBruiser());
        addCreatureReady(player1, new EmberBeast());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bruiser)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bruiser)).isEqualTo(4);
    }

}
