package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(InfiniteGuidelineStation.class)
class InfiniteGuidelineStationTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a tapped Robot for each multicolored permanent")
    void enteringCreatesTappedRobotsForMulticoloredPermanents() {
        harness.addToBattlefield(player1, multicoloredPermanent());
        harness.setHand(player1, List.of(new InfiniteGuidelineStation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> robots = findPermanents(player1, "Robot");
        assertThat(robots).hasSize(2);
        assertThat(robots).allSatisfy(robot -> {
            assertThat(robot.isTapped()).isTrue();
            assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        Permanent creature = addCreatureReady(player1, creature("Stationing Creature", 3, 3));

        harness.activateAbility(player1, battlefieldIndex(station), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(station.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At twelve charge counters, it becomes a flying artifact creature")
    void twelveCountersAnimateAndGrantFlying() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());

        station.setCounterCount(CounterType.CHARGE, 11);
        assertThat(gqs.isCreature(gd, station)).isFalse();
        assertThat(gqs.hasKeyword(gd, station, Keyword.FLYING)).isFalse();

        station.setCounterCount(CounterType.CHARGE, 12);
        assertThat(gqs.isCreature(gd, station)).isTrue();
        assertThat(gqs.hasKeyword(gd, station, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking draws for each multicolored permanent")
    void attackingDrawsForMulticoloredPermanents() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        station.setCounterCount(CounterType.CHARGE, 12);
        station.setSummoningSick(false);
        harness.addToBattlefield(player1, multicoloredPermanent());
        Card firstDraw = new Card();
        Card secondDraw = new Card();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        declareAttackers(List.of(battlefieldIndex(station)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Station needs another creature to activate")
    void stationNeedsAnotherCreature() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(station), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card multicoloredPermanent() {
        Card card = creature("Multicolored Permanent", 2, 2);
        card.setColor(CardColor.WHITE);
        card.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
        return card;
    }

    private Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
