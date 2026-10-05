package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantomTrain.class, GrizzlyBears.class, Spellbook.class})
class PhantomTrainTest extends BaseCardTest {

    @Test
    void sacrificingCreatureAddsCounterAndAnimatesTrain() {
        Permanent train = addReadyTrain();
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(train.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, train)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, train)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.getEffectivePower(gd, train)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, train)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void sacrificingArtifactAlsoPaysCost() {
        Permanent train = addReadyTrain();
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(train.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, train)).isTrue();
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    void animationEndsAtEndOfTurn() {
        Permanent train = addReadyTrain();
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, train)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, train)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, train)).doesNotContain(CardSubtype.SPIRIT);
    }

    @Test
    void cannotSacrificePhantomTrainItself() {
        addReadyTrain();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact or creature");
    }

    private Permanent addReadyTrain() {
        Permanent train = harness.addToBattlefieldAndReturn(player1, new PhantomTrain());
        train.setSummoningSick(false);
        return train;
    }

    @Test
    void sacrificeIsPaidBeforeCounterAndAnimationResolve() {
        Permanent train = harness.addToBattlefieldAndReturn(player1, new PhantomTrain());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(train.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, train)).isFalse();

        harness.passBothPriorities();

        assertThat(train.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, train)).isTrue();
        assertThat(train.isSummoningSick()).isTrue();
    }

    @Test
    void repeatedActivationsAccumulateCountersAndCountersSurviveCleanup() {
        Permanent train = addReadyTrain();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Spellbook());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(train.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, train)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, train)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, train)).contains(CardSubtype.SPIRIT);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, train)).isFalse();
        assertThat(train.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotPaySacrificeCostWithOpponentsPermanents() {
        addReadyTrain();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact or creature");

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Spellbook");
    }
}
