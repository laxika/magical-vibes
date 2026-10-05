package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlpharaelDreamingAcolyte;
import com.github.laxika.magicalvibes.cards.b.BiosynthicBurst;
import com.github.laxika.magicalvibes.cards.b.Bombard;
import com.github.laxika.magicalvibes.cards.b.BrightspearZealot;
import com.github.laxika.magicalvibes.cards.s.StarbreachWhale;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfiniteGuidelineStation.class, AlpharaelDreamingAcolyte.class,
        StarbreachWhale.class, BrightspearZealot.class, BiosynthicBurst.class, Bombard.class})
class InfiniteGuidelineStationTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a tapped Robot for each multicolored permanent")
    void enteringCreatesTappedRobotsForMulticoloredPermanents() {
        harness.addToBattlefield(player1, new AlpharaelDreamingAcolyte());
        harness.castFromHand(player1, new InfiniteGuidelineStation(), "{W}{U}{B}{R}{G}");
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
        Permanent creature = addCreatureReady(player1, new StarbreachWhale());

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
        harness.addToBattlefield(player1, new AlpharaelDreamingAcolyte());
        Card firstDraw = new BrightspearZealot();
        Card secondDraw = new StarbreachWhale();
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

    @Test
    void enteringCreatesArtifactCreatureRobots() {
        harness.castFromHand(player1, new InfiniteGuidelineStation(), "{W}{U}{B}{R}{G}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Robot")).hasSize(1).allSatisfy(robot -> {
            assertThat(gqs.isArtifact(gd, robot)).isTrue();
            assertThat(gqs.isCreature(gd, robot)).isTrue();
            assertThat(gqs.getEffectiveColors(gd, robot)).isEmpty();
        });
    }

    @Test
    void enteringCountsOnlyControlledMulticoloredPermanentsAtResolution() {
        harness.addToBattlefield(player1, new BrightspearZealot());
        harness.addToBattlefield(player2, new AlpharaelDreamingAcolyte());
        harness.setHand(player2, List.of(new Bombard()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castFromHand(player1, new InfiniteGuidelineStation(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new AlpharaelDreamingAcolyte());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Robot")).hasSize(2);
        assertThat(findPermanents(player2, "Robot")).isEmpty();
    }

    @Test
    void stationCanTapSummoningSickCreature() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StarbreachWhale());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(station), null, null);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(station.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void stationUsesPowerWhenAbilityResolves() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StarbreachWhale());
        harness.setHand(player1, List.of(new BiosynthicBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, battlefieldIndex(station), null, null);
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(station.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void animatedStationCannotTapItselfToStation() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        station.setCounterCount(CounterType.CHARGE, 12);
        station.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(station), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(station.isTapped()).isFalse();
        assertThat(station.getCounterCount(CounterType.CHARGE)).isEqualTo(12);
    }

    @Test
    void stationCannotUseTappedOrOpposingCreature() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StarbreachWhale());
        creature.tap();
        harness.addToBattlefield(player2, new BrightspearZealot());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(station), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(station.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotActivateDuringCombat() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StarbreachWhale());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(station), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void fallingBelowThresholdRemovesCreatureTypeAndFlying() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        station.setCounterCount(CounterType.CHARGE, 13);
        assertThat(gqs.isCreature(gd, station)).isTrue();
        assertThat(gqs.hasKeyword(gd, station, Keyword.FLYING)).isTrue();

        station.setCounterCount(CounterType.CHARGE, 11);

        assertThat(gqs.isCreature(gd, station)).isFalse();
        assertThat(gqs.hasKeyword(gd, station, Keyword.FLYING)).isFalse();
        assertThat(gqs.isArtifact(gd, station)).isTrue();
    }

    @Test
    void attackingCountsOnlyControlledMulticoloredPermanentsAtResolution() {
        Permanent station = addCreatureReady(player1, new InfiniteGuidelineStation());
        station.setCounterCount(CounterType.CHARGE, 12);
        harness.addToBattlefield(player1, new BrightspearZealot());
        harness.addToBattlefield(player2, new AlpharaelDreamingAcolyte());
        harness.setHand(player2, List.of(new Bombard()));
        harness.addMana(player2, ManaColor.RED, 3);
        Card firstDraw = new StarbreachWhale();
        Card secondDraw = new BrightspearZealot();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new BiosynthicBurst()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(battlefieldIndex(station)));
        harness.addToBattlefield(player1, new AlpharaelDreamingAcolyte());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void stationUsesLastKnownPowerWhenTappedCreatureDies() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AlpharaelDreamingAcolyte());
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(station), null, null);
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Alpharael, Dreaming Acolyte");
        assertThat(station.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void stationWithNegativePowerAddsNoCounters() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        station.setCounterCount(CounterType.CHARGE, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BrightspearZealot());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        harness.activateAbility(player1, battlefieldIndex(station), null, null);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(station.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
    }

    @Test
    void stationCannotActivateWithAnAbilityOnTheStack() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StarbreachWhale());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BrightspearZealot());
        harness.setHand(player1, List.of(new BiosynthicBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, battlefieldIndex(station), null, null);
        harness.handlePermanentChosen(player1, first.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(station), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(second.isTapped()).isFalse();
        resolveAllTriggers();
        assertThat(station.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void stationCanAddCountersAboveTheCreatureThreshold() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new InfiniteGuidelineStation());
        station.setCounterCount(CounterType.CHARGE, 12);
        harness.addToBattlefield(player1, new StarbreachWhale());

        harness.activateAbility(player1, battlefieldIndex(station), null, null);
        resolveAllTriggers();

        assertThat(station.getCounterCount(CounterType.CHARGE)).isEqualTo(15);
        assertThat(gqs.isCreature(gd, station)).isTrue();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
