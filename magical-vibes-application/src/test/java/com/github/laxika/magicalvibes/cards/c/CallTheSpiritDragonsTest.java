package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlabasterDragon;
import com.github.laxika.magicalvibes.cards.b.BoneDragon;
import com.github.laxika.magicalvibes.cards.b.BrimstoneDragon;
import com.github.laxika.magicalvibes.cards.b.BroodmateDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MistDragon;
import com.github.laxika.magicalvibes.cards.v.VolcanicDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallTheSpiritDragons.class, AlabasterDragon.class, BoneDragon.class, BrimstoneDragon.class, BroodmateDragon.class, CanopyDragon.class, GrizzlyBears.class, MistDragon.class, VolcanicDragon.class})
class CallTheSpiritDragonsTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your Dragons indestructible, but not an opponent's Dragons")
    void givesYourDragonsIndestructible() {
        harness.addToBattlefield(player1, new CallTheSpiritDragons());
        Permanent ownDragon = harness.addToBattlefieldAndReturn(player1, new AlabasterDragon());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new AlabasterDragon());

        assertThat(gqs.hasKeyword(gd, ownDragon, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentDragon, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Puts a counter on a Dragon of each color and wins with five different Dragons")
    void putsCountersOnDragonOfEachColorAndWins() {
        harness.addToBattlefield(player1, new CallTheSpiritDragons());
        Permanent white = harness.addToBattlefieldAndReturn(player1, new AlabasterDragon());
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new MistDragon());
        Permanent black = harness.addToBattlefieldAndReturn(player1, new BoneDragon());
        Permanent red = harness.addToBattlefieldAndReturn(player1, new VolcanicDragon());
        Permanent green = harness.addToBattlefieldAndReturn(player1, new CanopyDragon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(white.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(blue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(black.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(red.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(green.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not win when a color has no Dragon to receive a counter")
    void doesNotWinWithoutDragonOfEveryColor() {
        harness.addToBattlefield(player1, new CallTheSpiritDragons());
        Permanent white = harness.addToBattlefieldAndReturn(player1, new AlabasterDragon());
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new MistDragon());
        Permanent black = harness.addToBattlefieldAndReturn(player1, new BoneDragon());
        Permanent red = harness.addToBattlefieldAndReturn(player1, new VolcanicDragon());
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(white.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(blue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(black.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(red.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not win from putting multiple counters on one multicolored Dragon")
    void requiresFiveDifferentDragons() {
        harness.addToBattlefield(player1, new CallTheSpiritDragons());
        harness.addToBattlefield(player1, new AlabasterDragon());
        harness.addToBattlefield(player1, new MistDragon());
        Permanent multicoloredDragon = harness.addToBattlefieldAndReturn(player1, new BroodmateDragon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(multicoloredDragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Counts a counter placement completed through the Dragon choice prompt")
    void countsPlacementAfterChoosingAmongDragonsOfOneColor() {
        harness.addToBattlefield(player1, new CallTheSpiritDragons());
        harness.addToBattlefield(player1, new AlabasterDragon());
        harness.addToBattlefield(player1, new MistDragon());
        harness.addToBattlefield(player1, new BoneDragon());
        Permanent firstRed = harness.addToBattlefieldAndReturn(player1, new VolcanicDragon());
        Permanent secondRed = harness.addToBattlefieldAndReturn(player1, new BrimstoneDragon());
        harness.addToBattlefield(player1, new CanopyDragon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(secondRed.getId()));

        assertThat(firstRed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondRed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
