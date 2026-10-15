package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FlourishingHunter;
import com.github.laxika.magicalvibes.cards.g.GnarledGrovestrider;
import com.github.laxika.magicalvibes.cards.s.SporeCrawler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DormantGrove.class, GnarledGrovestrider.class, FlourishingHunter.class, SporeCrawler.class})
class DormantGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat puts a counter on a creature and transforms with sufficient toughness")
    void transformsWhenTargetHasAtLeastSixToughness() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FlourishingHunter());

        resolveBeginningOfCombat(hunter);

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grove.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Beginning of combat only puts a counter when the creature remains below six toughness")
    void doesNotTransformWhenTargetHasInsufficientToughness() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());

        resolveBeginningOfCombat(crawler);

        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grove.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The beginning-of-combat ability targets only a creature the controller controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new DormantGrove());
        harness.addToBattlefield(player1, new SporeCrawler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gnarled Grovestrider gives other creatures you control vigilance")
    void backFaceGrantsVigilanceToOtherOwnCreatures() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FlourishingHunter());
        resolveBeginningOfCombat(hunter);

        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SporeCrawler());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(grove.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The counter that raises toughness from five to six causes transformation")
    void transformsAtExactlySixAfterAddingCounter() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        crawler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        resolveBeginningOfCombat(crawler);

        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(grove.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("A different high-toughness creature does not satisfy the target's toughness condition")
    void onlyChecksTargetToughness() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new SporeCrawler());
        harness.addToBattlefield(player1, new FlourishingHunter());

        resolveBeginningOfCombat(crawler);

        assertThat(crawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grove.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("An illegal target at resolution prevents both the counter and transformation")
    void doesNothingWhenTargetChangesController() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FlourishingHunter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, hunter.getId());

        gd.playerBattlefields.get(player1.getId()).remove(hunter);
        gd.playerBattlefields.get(player2.getId()).add(hunter);
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(grove.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Dormant Grove does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FlourishingHunter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(grove.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The transformed face no longer puts counters on creatures at combat")
    void backFaceDoesNotRetainFrontFaceTrigger() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FlourishingHunter());
        resolveBeginningOfCombat(hunter);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grove.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Dormant Grove cannot transform when there are no legal creature targets")
    void doesNotTransformWithoutCreatures() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(grove.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Granted vigilance ends when Gnarled Grovestrider leaves the battlefield")
    void vigilanceGrantEndsWhenSourceLeaves() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new DormantGrove());
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new FlourishingHunter());
        resolveBeginningOfCombat(hunter);
        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(grove);

        assertThat(gqs.hasKeyword(gd, hunter, Keyword.VIGILANCE)).isFalse();
    }

    private void resolveBeginningOfCombat(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
