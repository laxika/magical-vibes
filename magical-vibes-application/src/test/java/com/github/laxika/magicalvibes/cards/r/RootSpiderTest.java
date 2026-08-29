package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootSpider.class, BeastWalkers.class})
class RootSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives Root Spider +1/+0 and first strike")
    void blockingBoostsAndGrantsFirstStrike() {
        addCreatureReady(player1, new BeastWalkers()).setAttacking(true);
        Permanent spider = addCreatureReady(player2, new RootSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(1);
        assertThat(spider.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Becoming blocked does not trigger Root Spider")
    void becomingBlockedDoesNothing() {
        Permanent spider = addCreatureReady(player1, new RootSpider());
        spider.setAttacking(true);
        addCreatureReady(player2, new BeastWalkers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The blocking boost and first strike wear off at end of turn")
    void blockingBoostAndFirstStrikeExpireAtEndOfTurn() {
        addCreatureReady(player1, new BeastWalkers()).setAttacking(true);
        Permanent spider = addCreatureReady(player2, new RootSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isFalse();
    }
}
