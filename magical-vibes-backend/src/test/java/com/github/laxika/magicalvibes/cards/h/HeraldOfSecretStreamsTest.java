package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeraldOfSecretStreamsTest extends BaseCardTest {

    // ===== Creatures with +1/+1 counters can't be blocked =====

    @Test
    @DisplayName("Creature with +1/+1 counter can't be blocked")
    void creatureWithCounterCantBeBlocked() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        bears.setPlusOnePlusOneCounters(1);

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Creature without +1/+1 counter can still be blocked")
    void creatureWithoutCounterCanBeBlocked() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    // ===== Herald itself benefits if it has counters =====

    @Test
    @DisplayName("Herald itself can't be blocked if it has +1/+1 counters")
    void heraldWithCounterCantBeBlocked() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());

        Permanent herald = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Herald of Secret Streams"))
                .findFirst().orElseThrow();

        herald.setPlusOnePlusOneCounters(1);

        assertThat(gqs.hasCantBeBlocked(gd, herald)).isTrue();
    }

    @Test
    @DisplayName("Herald without counters can be blocked")
    void heraldWithoutCounterCanBeBlocked() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());

        Permanent herald = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Herald of Secret Streams"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasCantBeBlocked(gd, herald)).isFalse();
    }

    // ===== Does not affect opponent's creatures =====

    @Test
    @DisplayName("Does not affect opponent's creature with +1/+1 counter")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        opponentBears.setPlusOnePlusOneCounters(1);

        assertThat(gqs.hasCantBeBlocked(gd, opponentBears)).isFalse();
    }

    // ===== Effect removed when Herald leaves =====

    @Test
    @DisplayName("Effect removed when Herald leaves the battlefield")
    void effectRemovedWhenHeraldLeaves() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        bears.setPlusOnePlusOneCounters(1);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Herald of Secret Streams"));

        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }

    // ===== Counter removed: creature can be blocked again =====

    @Test
    @DisplayName("Creature can be blocked again after counters are removed")
    void creatureCanBeBlockedAfterCountersRemoved() {
        harness.addToBattlefield(player1, new HeraldOfSecretStreams());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        bears.setPlusOnePlusOneCounters(2);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();

        bears.setPlusOnePlusOneCounters(0);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isFalse();
    }
}
