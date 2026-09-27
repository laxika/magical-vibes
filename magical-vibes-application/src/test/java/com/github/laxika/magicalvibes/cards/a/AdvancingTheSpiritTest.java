package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.q.QuicksilverBrashBlur;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdvancingTheSpirit.class, QuicksilverBrashBlur.class})
class AdvancingTheSpiritTest extends BaseCardTest {

    @Test
    void drawsACardWhenItEnters() {
        Card drawnCard = new QuicksilverBrashBlur();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player1, new AdvancingTheSpirit());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void makesTheFirstPowerUpActivationFreeAndOnlyThatActivation() {
        harness.addToBattlefield(player1, new AdvancingTheSpirit());
        Permanent firstQuicksilver = addCreatureReady(player1, new QuicksilverBrashBlur());
        Permanent secondQuicksilver = addCreatureReady(player1, new QuicksilverBrashBlur());
        Permanent thirdQuicksilver = addCreatureReady(player1, new QuicksilverBrashBlur());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstQuicksilver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(secondQuicksilver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 3, null, null);
        harness.passBothPriorities();

        assertThat(thirdQuicksilver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
