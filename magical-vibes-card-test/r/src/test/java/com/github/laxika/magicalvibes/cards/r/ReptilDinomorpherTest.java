package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ReptilDinomorpher.class)
class ReptilDinomorpherTest extends BaseCardTest {

    @Test
    void brontosaurusAbilitySetsStatsTypesAndKeywordsUntilEndOfTurn() {
        Permanent reptil = harness.addToBattlefieldAndReturn(player1, new ReptilDinomorpher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reptil)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, reptil)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, reptil))
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.HERO);
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, reptil, CardSubtype.DINOSAUR)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void tyrannosaurusRexAbilitySetsStatsTypesAndTrampleUntilEndOfTurn() {
        Permanent reptil = harness.addToBattlefieldAndReturn(player1, new ReptilDinomorpher());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reptil)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, reptil)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, reptil))
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.HERO);
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, reptil, CardSubtype.DINOSAUR)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.TRAMPLE)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0, 1, 3, 5", "1, 0, 6, 6"})
    void lastAbilityToResolveSetsStatsWhileBothFormsKeywordsRemain(
            int firstAbility, int secondAbility, int expectedPower, int expectedToughness) {
        Permanent reptil = harness.addToBattlefieldAndReturn(player1, new ReptilDinomorpher());
        int originalPower = gqs.getEffectivePower(gd, reptil);
        int originalToughness = gqs.getEffectiveToughness(gd, reptil);
        var originalSubtypes = java.util.List.copyOf(gqs.effectiveCreatureSubtypes(gd, reptil));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.activateAbility(player1, 0, firstAbility, null, null);
        harness.activateAbility(player1, 0, secondAbility, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reptil)).isEqualTo(expectedPower);
        assertThat(gqs.getEffectiveToughness(gd, reptil)).isEqualTo(expectedToughness);
        assertThat(gqs.effectiveCreatureSubtypes(gd, reptil))
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.HERO);
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.TRAMPLE)).isTrue();
        assertThat(reptil.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reptil)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, reptil)).isEqualTo(originalToughness);
        assertThat(gqs.effectiveCreatureSubtypes(gd, reptil))
                .containsExactlyInAnyOrderElementsOf(originalSubtypes);
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, reptil, Keyword.TRAMPLE)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0, 3, 3, 5", "1, 6, 6, 6"})
    void transformationPreservesCountersAndCanBeActivatedWhileTapped(
            int abilityIndex, int manaCost, int basePower, int baseToughness) {
        Permanent reptil = harness.addToBattlefieldAndReturn(player1, new ReptilDinomorpher());
        reptil.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        reptil.tap();
        int originalPower = gqs.getEffectivePower(gd, reptil);
        int originalToughness = gqs.getEffectiveToughness(gd, reptil);
        harness.addMana(player1, ManaColor.COLORLESS, manaCost);

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reptil)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, reptil)).isEqualTo(baseToughness + 2);
        assertThat(reptil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(reptil.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, reptil)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, reptil)).isEqualTo(originalToughness);
        assertThat(reptil.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
