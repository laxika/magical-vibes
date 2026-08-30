package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BumiKingOfThreeTrials.class, AirbendingLesson.class, Forest.class})
class BumiKingOfThreeTrialsTest extends BaseCardTest {

    @Test
    void noLessonsMeansNoModesAreChosen() {
        Permanent bumi = castBumi();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void oneLessonAllowsOneMode() {
        Permanent bumi = castBumi(new AirbendingLesson());

        harness.handleListChoice(player1, "Put three +1/+1 counters on Bumi.");
        harness.passBothPriorities();

        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void threeLessonsAllowTwoModesAndEarthbendTheChosenLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bumi = castBumi(new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson());

        harness.handleListChoice(player1, "Put three +1/+1 counters on Bumi.");
        harness.handleListChoice(player1, "Earthbend 3.");
        harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(bumi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent castBumi(Card... lessons) {
        harness.setGraveyard(player1, List.of(lessons));
        harness.setHand(player1, List.of(new BumiKingOfThreeTrials()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BumiKingOfThreeTrials)
                .findFirst()
                .orElseThrow();
    }
}
