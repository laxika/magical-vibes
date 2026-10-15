package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NoWayOut;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ChampionOfThePerished.class, Gravecrawler.class, GrizzlyBears.class, NoWayOut.class})
class ChampionOfThePerishedTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when another Zombie enters the battlefield")
    void getsCounterWhenZombieEnters() {
        harness.addToBattlefield(player1, new ChampionOfThePerished());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        castCreature(player1, new Gravecrawler(), "{B}");

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when a non-Zombie creature enters")
    void noCounterWhenNonZombieEnters() {
        harness.addToBattlefield(player1, new ChampionOfThePerished());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        castCreature(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's Zombie enters")
    void noCounterWhenOpponentsZombieEnters() {
        harness.addToBattlefield(player1, new ChampionOfThePerished());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Gravecrawler(), "{B}");
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets one counter for each Zombie that enters")
    void getsMultipleCounters() {
        harness.addToBattlefield(player1, new ChampionOfThePerished());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        castCreature(player1, new Gravecrawler(), "{B}");
        castCreature(player1, new Gravecrawler(), "{B}");

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new ChampionOfThePerished(), "{B}");
        harness.passBothPriorities();

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A second Champion triggers only the Champion already on the battlefield")
    void secondChampionTriggersFirstChampion() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ChampionOfThePerished());
        harness.castFromHand(player1, new ChampionOfThePerished(), "{B}");
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Zombie token entering triggers the Champion")
    void zombieTokenTriggersChampion() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfThePerished());
        harness.setHand(player1, List.of(new NoWayOut()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castCreature(Player player, Card card, String manaCost) {
        harness.castFromHand(player, card, manaCost);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
