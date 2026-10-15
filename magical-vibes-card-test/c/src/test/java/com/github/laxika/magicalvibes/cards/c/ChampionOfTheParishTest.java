package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ChampionOfTheParish.class, DoomedTraveler.class, AmbushViper.class, GatherTheTownsfolk.class})
class ChampionOfTheParishTest extends BaseCardTest {
    @Test
    @DisplayName("Does not trigger for its own entry")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new ChampionOfTheParish()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Champion triggers the first but not itself")
    void secondChampionTriggersFirstOnly() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ChampionOfTheParish());
        harness.setHand(player1, List.of(new ChampionOfTheParish()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Human token entering generates its own counter trigger")
    void humanTokensEachTrigger() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfTheParish());
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Human entering without being cast still triggers")
    void humanEnteringWithoutCastTriggers() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfTheParish());
        harness.enterBattlefieldAndReturn(player1, new DoomedTraveler());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }


    @Test
    @DisplayName("Gets a +1/+1 counter when another Human enters the battlefield")
    void getsCounterWhenHumanEnters() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfTheParish());
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Cast Doomed Traveler (Human Soldier)
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (triggers Champion)
        harness.passBothPriorities(); // resolve Champion's +1/+1 counter triggered ability

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when a non-Human creature enters")
    void noCounterWhenNonHumanEnters() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfTheParish());

        // Cast Ambush Viper (Snake, not Human)
        harness.setHand(player1, List.of(new AmbushViper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when opponent casts a Human")
    void noCounterWhenOpponentCastsHuman() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfTheParish());

        // Opponent casts a Human
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DoomedTraveler()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets multiple counters from multiple Human entries")
    void getsMultipleCounters() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new ChampionOfTheParish());

        // Cast first Human
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Champion's triggered ability

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Cast second Human
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Champion's triggered ability

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }
}
