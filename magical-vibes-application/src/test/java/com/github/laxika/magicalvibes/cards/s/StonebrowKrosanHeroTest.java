package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StonebrowKrosanHero.class, GrizzlyBears.class})
class StonebrowKrosanHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Stonebrow gets +2/+2")
    void attackingStonebrowIsBoosted() {
        Permanent stonebrow = addCreatureReady(player1, new StonebrowKrosanHero());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, stonebrow)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, stonebrow)).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature without trample does not trigger Stonebrow's ability")
    void nonTramplingAttackerIsNotBoosted() {
        addCreatureReady(player1, new StonebrowKrosanHero());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Stonebrow's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent stonebrow = addCreatureReady(player1, new StonebrowKrosanHero());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, stonebrow)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stonebrow)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stonebrow)).isEqualTo(4);
    }
}
