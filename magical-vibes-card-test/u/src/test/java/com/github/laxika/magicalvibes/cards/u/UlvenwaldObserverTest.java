package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlvenwaldObserver.class, Forest.class, GiantSpider.class, GrizzlyBears.class,
        Murder.class, WrathOfGod.class, GloriousAnthem.class})
class UlvenwaldObserverTest extends BaseCardTest {

    @Test
    void drawsWhenCreatureWithToughnessAtLeastFourYouControlDies() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player1, new GiantSpider());

        destroy(player1, player1, "Giant Spider");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotDrawWhenCreatureWithToughnessBelowFourYouControlDies() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroy(player1, player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenUlvenwaldObserverItselfDies() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new UlvenwaldObserver());

        destroy(player1, player1, "Ulvenwald Observer");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotDrawWhenOpponentsCreatureDies() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player2, new GiantSpider());

        destroy(player1, player2, "Giant Spider");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenCountersRaiseDyingCreaturesToughnessToFour() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        destroy(player1, player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotDrawWhenCountersReduceDyingCreaturesToughnessBelowFour() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        spider.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        destroy(player1, player1, "Giant Spider");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawForItsOwnDeathWithToughnessBelowFour() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent observer = harness.addToBattlefieldAndReturn(player1, new UlvenwaldObserver());
        observer.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        destroy(player1, player1, "Ulvenwald Observer");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsForEachQualifyingCreatureWhenObserverDiesSimultaneously() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void eachObserverDrawsForTheSameQualifyingDeath() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Giant Spider"));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void usesToughnessBeforeDeathIncludingControllerDependentStaticBonuses() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new UlvenwaldObserver());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        destroy(player1, player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void usesItsOwnToughnessBeforeDeathIncludingControllerDependentStaticBonuses() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent observer = harness.addToBattlefieldAndReturn(player1, new UlvenwaldObserver());
        observer.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        assertThat(gqs.getEffectiveToughness(gd, observer)).isEqualTo(4);

        destroy(player1, player1, "Ulvenwald Observer");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private void destroy(Player caster, Player targetController, String targetName) {
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
