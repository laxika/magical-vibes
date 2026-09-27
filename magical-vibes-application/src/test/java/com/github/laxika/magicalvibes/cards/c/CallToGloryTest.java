package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EiganjoCastle;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallToGlory.class, MothriderSamurai.class, LanternKami.class, EiganjoCastle.class})
class CallToGloryTest extends BaseCardTest {

    @Test
    void untapsOwnCreaturesAndBoostsSamurai() {
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());
        Permanent nonSamurai = addCreatureReady(player1, new LanternKami());
        Permanent opponent = addCreatureReady(player2, new LanternKami());
        samurai.tap();
        nonSamurai.tap();
        opponent.tap();

        harness.castFromHand(player1, new CallToGlory(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(samurai.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
        assertThat(nonSamurai.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, nonSamurai)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonSamurai)).isEqualTo(1);
        assertThat(opponent.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(1);
    }

    @Test
    void doesNotUntapNoncreatures() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());
        Permanent creature = addCreatureReady(player1, new LanternKami());
        land.tap();
        creature.tap();

        harness.castFromHand(player1, new CallToGlory(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void samuraiBoostWearsOffAtCleanup() {
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());

        harness.castFromHand(player1, new CallToGlory(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(2);
    }
}
