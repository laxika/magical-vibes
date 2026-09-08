package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkywardSpider.class, GiantGrowth.class, Shock.class})
class SkywardSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying only while modified")
    void hasFlyingOnlyWhileModified() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SkywardSpider());

        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();

        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isTrue();

        spider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay {2}")
    void wardCountersUnpaidSpell() {
        Permanent spider = addSpider();
        prepareOpponentCast();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spider.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Paying {2} lets an opponent's spell targeting it resolve")
    void payingWardManaLetsSpellResolve() {
        Permanent spider = addSpider();
        prepareOpponentCast();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, spider.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
    }

    private Permanent addSpider() {
        return harness.addToBattlefieldAndReturn(player1, new SkywardSpider());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
