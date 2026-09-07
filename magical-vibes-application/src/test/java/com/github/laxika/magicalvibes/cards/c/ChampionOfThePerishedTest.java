package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ChampionOfThePerished.class, Gravecrawler.class, GrizzlyBears.class})
class ChampionOfThePerishedTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when another Zombie enters the battlefield")
    void getsCounterWhenZombieEnters() {
        harness.addToBattlefield(player1, new ChampionOfThePerished());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        castCreature(player1, new Gravecrawler(), ManaColor.BLACK, 1);

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when a non-Zombie creature enters")
    void noCounterWhenNonZombieEnters() {
        harness.addToBattlefield(player1, new ChampionOfThePerished());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        castCreature(player1, new GrizzlyBears(), ManaColor.GREEN, 2);

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
        harness.setHand(player2, List.of(new Gravecrawler()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets one counter for each Zombie that enters")
    void getsMultipleCounters() {
        harness.addToBattlefield(player1, new ChampionOfThePerished());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        castCreature(player1, new Gravecrawler(), ManaColor.BLACK, 1);
        castCreature(player1, new Gravecrawler(), ManaColor.BLACK, 1);

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castCreature(Player player, Card card, ManaColor manaColor, int amount) {
        harness.setHand(player, List.of(card));
        harness.addMana(player, manaColor, amount);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
