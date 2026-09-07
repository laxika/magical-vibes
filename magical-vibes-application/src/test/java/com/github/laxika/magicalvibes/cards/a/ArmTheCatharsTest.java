package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({ArmTheCathars.class, GrizzlyBears.class, Plains.class})
class ArmTheCatharsTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Applies the three different boosts and vigilance to three targets")
    void appliesDifferentBoostsToThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmTheCathars()));
        addMana();

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, third, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The second and third targets are optional")
    void optionalTargetsMayBeOmitted() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmTheCathars()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Boosts and vigilance expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmTheCathars()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Targets must be distinct creatures")
    void rejectsRepeatedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArmTheCathars()));
        addMana();

        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(targetId, targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new ArmTheCathars()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
