package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuntMay.class, GiantSpider.class, GrizzlyBears.class})
class AuntMayTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering gains you 1 life")
    void anotherCreatureEnteringGainsLife() {
        harness.addToBattlefield(player1, new AuntMay());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveCreatureAndTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("A Spider entering gains you 1 life and gets a +1/+1 counter")
    void spiderEnteringGainsLifeAndGetsCounter() {
        harness.addToBattlefield(player1, new AuntMay());
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveCreatureAndTriggers();

        Permanent spider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GiantSpider)
                .findFirst()
                .orElseThrow();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature does not trigger Aunt May")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new AuntMay());
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantSpider()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent spider = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GiantSpider)
                .findFirst()
                .orElseThrow();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveCreatureAndTriggers() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
