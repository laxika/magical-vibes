package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LifecraftAwakening;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BotanicalBrawler.class, BondBeetle.class, DarksteelRelic.class, GrizzlyBears.class,
        LifecraftAwakening.class})
class BotanicalBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent brawler = castBrawler(player1);

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets one counter for the first counter placement on each other permanent each turn")
    void triggersOncePerOtherPermanent() {
        Permanent brawler = castBrawler(player1);
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBondBeetle(player1, firstBear);
        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        castBondBeetle(player1, firstBear);
        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        castBondBeetle(player1, secondBear);
        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Triggers for a noncreature permanent")
    void triggersForNoncreaturePermanent() {
        Permanent brawler = castBrawler(player1);
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());

        harness.setHand(player1, List.of(new LifecraftAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, 1, relic.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters placed before Brawler enters still count for the turn")
    void countersPlacedBeforeBrawlerEntersCount() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        putCounterOnArtifact(relic);

        Permanent brawler = castBrawler(player1);
        putCounterOnArtifact(relic);
        harness.passBothPriorities();

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Putting counters on Brawler itself does not trigger its ability")
    void doesNotTriggerForItsOwnCounters() {
        Permanent brawler = castBrawler(player1);

        castBondBeetle(player1, brawler);

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Brawler entering with counters triggers once and cannot trigger again that turn")
    void anotherBrawlerEnteringWithCountersTriggersOnce() {
        Permanent first = castBrawler(player1);
        harness.castFromHand(player1, new BotanicalBrawler(), "{G}{W}");
        harness.passBothPriorities();
        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        castBondBeetle(player1, second);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Several counters put on a permanent at once trigger only once")
    void multipleCountersTriggerOnlyOnce() {
        Permanent brawler = castBrawler(player1);
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new LifecraftAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, 3, relic.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("First placement tracking resets on the opponent's turn")
    void triggersAgainOnOpponentsTurn() {
        Permanent brawler = castBrawler(player1);
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        putCounterOnArtifact(relic);
        harness.passBothPriorities();
        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        putCounterOnArtifact(relic);
        harness.passBothPriorities();

        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's permanent entering with counters does not trigger Brawler")
    void opponentsCountersDoNotTrigger() {
        Permanent brawler = castBrawler(player1);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        Permanent opponentBrawler = castBrawler(player2);

        assertThat(brawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentBrawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
    private void castBondBeetle(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new BondBeetle()));
        harness.addMana(caster, ManaColor.GREEN, 2);
        harness.castCreature(caster, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent castBrawler(Player player) {
        harness.castFromHand(player, new BotanicalBrawler(), "{G}{W}");
        harness.passBothPriorities();
        return findPermanent(player, "Botanical Brawler");
    }

    private void putCounterOnArtifact(Permanent artifact) {
        harness.setHand(player1, List.of(new LifecraftAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, 1, artifact.getId());
        harness.passBothPriorities();
    }
}
