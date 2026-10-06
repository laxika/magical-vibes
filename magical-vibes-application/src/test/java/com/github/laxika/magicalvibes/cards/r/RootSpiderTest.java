package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.h.HighGround;
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

@CardUsed({RootSpider.class, BeastWalkers.class, HighGround.class})
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

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(spider.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The blocking ability waits for priority before granting its bonuses")
    void blockingBonusesWaitForResolution() {
        addCreatureReady(player1, new BeastWalkers()).setAttacking(true);
        Permanent spider = addCreatureReady(player2, new RootSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isFalse();

        resolveAllTriggers();

        assertThat(spider.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Root Spider kills a blocked attacker before it can deal normal combat damage")
    void grantedFirstStrikePreventsReturnDamage() {
        addCreatureReady(player1, new BeastWalkers());
        Permanent spider = addCreatureReady(player2, new RootSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();

        harness.assertOnBattlefield(player2, "Root Spider");
        harness.assertInGraveyard(player1, "Beast Walkers");
        assertThat(spider.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({RootSpider.class, BeastWalkers.class, HighGround.class})
    @DisplayName("Blocking two creatures triggers Root Spider only once")
    void blockingMultipleCreaturesGrantsOnlyOneBoost() {
        addCreatureReady(player1, new BeastWalkers()).setAttacking(true);
        addCreatureReady(player1, new BeastWalkers()).setAttacking(true);
        harness.addToBattlefield(player2, new HighGround());
        Permanent spider = addCreatureReady(player2, new RootSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        assertThat(spider.getPowerModifier()).isEqualTo(1);
        assertThat(spider.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, spider, Keyword.FIRST_STRIKE)).isTrue();
    }
}
