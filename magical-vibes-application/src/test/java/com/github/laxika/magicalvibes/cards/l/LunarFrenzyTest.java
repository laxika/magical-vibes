package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LunarFrenzy.class, FountainOfYouth.class, GrizzlyBears.class})
class LunarFrenzyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature you control +X/+0, first strike, and trample")
    void boostsCreatureAndGrantsKeywords() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LunarFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantForX(player1, 0, 3, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("X=0 still grants first strike and trample")
    void zeroXStillGrantsKeywords() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LunarFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForX(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LunarFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForX(player1, 0, 2, List.of(bear.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LunarFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new LunarFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 0, List.of(fountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies on the stack retain their own X values and targets")
    void stackedCopiesKeepSeparateXValuesAndTargets() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LunarFrenzy(), new LunarFrenzy()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantForX(player1, 0, 1, List.of(firstBear.getId()));
        harness.castInstantForX(player1, 0, 3, List.of(secondBear.getId()));
        harness.passBothPriorities();

        assertThat(secondBear.getEffectivePower()).isEqualTo(5);
        assertThat(secondBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(secondBear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(secondBear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(firstBear.getEffectivePower()).isEqualTo(2);
        assertThat(firstBear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(firstBear.hasKeyword(Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(firstBear.getEffectivePower()).isEqualTo(3);
        assertThat(firstBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(firstBear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(firstBear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(secondBear.getEffectivePower()).isEqualTo(5);
    }
}
