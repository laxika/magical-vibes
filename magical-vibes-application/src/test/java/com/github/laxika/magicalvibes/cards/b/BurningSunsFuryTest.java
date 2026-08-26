package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningSunsFury.class, GrizzlyBears.class, Mountain.class})
class BurningSunsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives up to two target creatures +2/+0 and haste")
    void boostsTwoTargetsAndGrantsHaste() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(first.getId(), second.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(first.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(second.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Allows one target or no targets")
    void allowsFewerThanTwoTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.setHand(player1, List.of(new BurningSunsFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The boost and haste wear off at cleanup")
    void wearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(List.of(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Convoke can pay the generic mana cost")
    void castsWithConvoke() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurningSunsFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(target.getId()), List.of(convokeCreature.getId()));
        assertThat(convokeCreature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new BurningSunsFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new BurningSunsFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
