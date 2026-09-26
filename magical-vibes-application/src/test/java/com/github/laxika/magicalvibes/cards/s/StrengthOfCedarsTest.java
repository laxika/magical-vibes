package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({StrengthOfCedars.class, Forest.class, Mountain.class, IsamaruHoundOfKonda.class,
        SenseisDiviningTop.class})
class StrengthOfCedarsTest extends BaseCardTest {

    @Test
    @DisplayName("Boost equals the number of lands the controller controls")
    void boostEqualsControlledLandCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new StrengthOfCedars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent's lands are not counted")
    void opponentLandsAreNotCounted() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StrengthOfCedars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("With no lands the boost is zero")
    void noLandsGivesNoBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new StrengthOfCedars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts lands when the spell resolves")
    void countsLandsAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new StrengthOfCedars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can boost a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new StrengthOfCedars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new StrengthOfCedars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SenseisDiviningTop());
        harness.setHand(player1, List.of(new StrengthOfCedars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
