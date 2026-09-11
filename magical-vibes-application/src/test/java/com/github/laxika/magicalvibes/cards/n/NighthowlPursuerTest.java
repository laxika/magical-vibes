package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NighthowlPursuer.class, CrawWurm.class})
class NighthowlPursuerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a ferocious boost without a creature with power 4 or greater")
    void doesNotBoostWithoutFerocious() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 when it attacks while its controller has a creature with power 4 or greater")
    void boostsWithFerocious() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        addCreatureReady(player1, new CrawWurm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ferocious boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent pursuer = addCreatureReady(player1, new NighthowlPursuer());
        addCreatureReady(player1, new CrawWurm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pursuer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pursuer)).isEqualTo(1);
    }
}
