package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirstTimeFlyer.class, AirbendingLesson.class, GrizzlyBears.class})
class FirstTimeFlyerTest extends BaseCardTest {

    @Test
    void hasBaseStatsWithoutLessonInControllerGraveyard() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new FirstTimeFlyer());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(2);
    }

    @Test
    void getsPlusOnePlusOneWithLessonInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new FirstTimeFlyer());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(3);
    }

    @Test
    void nonLessonAndOpponentLessonCardsDoNotCount() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new AirbendingLesson()));
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new FirstTimeFlyer());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(2);
    }

    @Test
    void boostUpdatesWhenControllerGraveyardChanges() {
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new FirstTimeFlyer());

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(2);
    }
}
