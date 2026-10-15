package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({XolatoyacTheSmilingFlood.class, Forest.class, GrizzlyBears.class})
class XolatoyacTheSmilingFloodTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a flood counter on a land and makes it an Island")
    void etbFloodsTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new XolatoyacTheSmilingFlood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, forest.getId());
        resolveAllTriggers();

        assertThat(forest.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactlyInAnyOrder(CardSubtype.FOREST, CardSubtype.ISLAND);

        forest.setCounterCount(CounterType.FLOOD, 0);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Attacking puts a flood counter on a target land")
    void attackFloodsTargetLand() {
        Permanent xolatoyac = addCreatureReady(player1, new XolatoyacTheSmilingFlood());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(xolatoyac.isTapped()).isTrue();
        assertThat(forest.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).contains(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("End step untaps each controlled permanent with any counter")
    void endStepUntapsCounteredPermanentsYouControl() {
        harness.addToBattlefield(player1, new XolatoyacTheSmilingFlood());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent counteredBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent uncounteredBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        forest.setCounterCount(CounterType.FLOOD, 1);
        counteredBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentBear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        forest.tap();
        counteredBear.tap();
        uncounteredBear.tap();
        opponentBear.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            resolveAllTriggers();
        });

        assertThat(forest.isTapped()).isFalse();
        assertThat(counteredBear.isTapped()).isFalse();
        assertThat(uncounteredBear.isTapped()).isTrue();
        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the last flood counter permanently ends the Island effect")
    void islandEffectDoesNotResumeWhenAnotherFloodCounterIsAdded() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new XolatoyacTheSmilingFlood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, forest.getId());
        resolveAllTriggers();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).contains(CardSubtype.ISLAND);
        forest.setCounterCount(CounterType.FLOOD, 2);
        forest.setCounterCount(CounterType.FLOOD, 1);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).contains(CardSubtype.ISLAND);
        forest.setCounterCount(CounterType.FLOOD, 0);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).doesNotContain(CardSubtype.ISLAND);
        forest.setCounterCount(CounterType.FLOOD, 1);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Untapping checks counters when the end-step trigger resolves")
    void endStepChecksCountersAtResolution() {
        harness.addToBattlefield(player1, new XolatoyacTheSmilingFlood());
        Permanent losingCounter = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent gainingCounter = harness.addToBattlefieldAndReturn(player1, new Forest());
        losingCounter.setCounterCount(CounterType.FLOOD, 1);
        losingCounter.tap();
        gainingCounter.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            assertThat(gd.stack).hasSize(1);
            losingCounter.setCounterCount(CounterType.FLOOD, 0);
            gainingCounter.setCounterCount(CounterType.FLOOD, 1);
            resolveAllTriggers();
        });

        assertThat(losingCounter.isTapped()).isTrue();
        assertThat(gainingCounter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap ability does not trigger at an opponent's end step")
    void opponentEndStepDoesNotUntap() {
        harness.addToBattlefield(player1, new XolatoyacTheSmilingFlood());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.FLOOD, 1);
        forest.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            resolveAllTriggers();
        });

        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A stun counter replaces the end-step untap")
    void endStepRemovesStunCounterInsteadOfUntapping() {
        harness.addToBattlefield(player1, new XolatoyacTheSmilingFlood());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.STUN, 1);
        forest.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(TurnStep.END_STEP);
            resolveAllTriggers();
        });

        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("The trigger can target only lands")
    void etbRequiresLandTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new XolatoyacTheSmilingFlood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
