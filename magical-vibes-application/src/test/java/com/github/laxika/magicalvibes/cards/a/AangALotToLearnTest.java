package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AangALotToLearn.class, AirbendingLesson.class, GrizzlyBears.class, Shock.class, LightningStrike.class})
class AangALotToLearnTest extends BaseCardTest {

    @Test
    void hasVigilanceOnlyWithLessonInControllerGraveyard() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangALotToLearn());

        assertThat(gqs.hasKeyword(gd, aang, Keyword.VIGILANCE)).isFalse();

        harness.setGraveyard(player1, List.of(new AirbendingLesson()));

        assertThat(gqs.hasKeyword(gd, aang, Keyword.VIGILANCE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new AirbendingLesson()));

        assertThat(gqs.hasKeyword(gd, aang, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void putsCounterOnAangWhenAnotherCreatureYouControlDies() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangALotToLearn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(aang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOpponentCreatureDeath() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangALotToLearn());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(aang.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
