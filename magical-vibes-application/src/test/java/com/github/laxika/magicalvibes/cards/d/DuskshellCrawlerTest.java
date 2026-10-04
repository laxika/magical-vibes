package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskshellCrawler.class, DeepwoodDenizen.class})
class DuskshellCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and puts a +1/+1 counter on target creature")
    void entersAndPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DeepwoodDenizen());

        harness.setHand(player1, List.of(new DuskshellCrawler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives trample to own creatures with +1/+1 counters")
    void givesTrampleToOwnCounteredCreatures() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new DuskshellCrawler());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player1, new DeepwoodDenizen());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new DeepwoodDenizen());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DeepwoodDenizen());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, crawler, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Removes granted trample when the +1/+1 counter is removed")
    void removesTrampleWhenCounterIsRemoved() {
        harness.addToBattlefieldAndReturn(player1, new DuskshellCrawler());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeepwoodDenizen());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Crawler grants itself trample when it has a +1/+1 counter")
    void grantsItselfTrampleWithCounter() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new DuskshellCrawler());

        assertThat(gqs.hasKeyword(gd, crawler, Keyword.TRAMPLE)).isFalse();

        crawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, crawler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Other counter types do not qualify a creature for trample")
    void doesNotGrantTrampleForOtherCounters() {
        harness.addToBattlefieldAndReturn(player1, new DuskshellCrawler());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeepwoodDenizen());
        creature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Granted trample ends when the Crawler leaves the battlefield")
    void losesTrampleWhenCrawlerLeaves() {
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new DuskshellCrawler());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeepwoodDenizen());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(crawler);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter counter immediately gives an own creature trample")
    void enterCounterGrantsTrampleToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DeepwoodDenizen());
        harness.setHand(player1, List.of(new DuskshellCrawler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }
}
