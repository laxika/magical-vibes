package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldfuryStrider.class, GrizzlyBears.class, IzzetCluestone.class})
class GoldfuryStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two artifacts and/or creatures gives target creature +2/+0")
    void tapsTwoPermanentsAndBoostsTargetCreature() {
        Permanent strider = addStrider();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, strider.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(strider.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The power boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent strider = addStrider();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, strider.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability requires sorcery timing")
    void activationRequiresSorceryTiming() {
        Permanent strider = addStrider();
        harness.addToBattlefield(player1, new IzzetCluestone());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(strider.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An artifact that is not a creature is an illegal target")
    void rejectsNonCreatureTarget() {
        Permanent strider = addStrider();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(strider.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(secondArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick artifact creatures can pay the cost and the source can target itself")
    void summoningSickCreaturesCanPayCostToBoostSource() {
        Permanent strider = addStrider();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());
        assertThat(strider.isSummoningSick()).isTrue();
        assertThat(other.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, 0, null, strider.getId());
        harness.passBothPriorities();

        assertThat(strider.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, strider)).isEqualTo(5);
    }

    @Test
    @DisplayName("A single artifact creature does not count as two permanents")
    void requiresTwoDistinctPermanents() {
        Permanent strider = addStrider();
        harness.addToBattlefield(player2, new GoldfuryStrider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, strider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
        assertThat(strider.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Strider can activate by tapping two other permanents and target an opponent's creature")
    void tappedSourceCanBoostOpponentsCreature() {
        Permanent strider = addStrider();
        strider.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldfuryStrider());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Already-tapped permanents cannot pay the activation cost")
    void tappedPermanentsCannotPayCost() {
        Permanent strider = addStrider();
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());
        other.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, strider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
        assertThat(strider.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sorcery timing prevents another activation while the first ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent strider = addStrider();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());

        harness.activateAbility(player1, 0, 0, null, strider.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, strider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(strider.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(5);
    }

    private Permanent addStrider() {
        return harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());
    }
}
