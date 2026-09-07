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

    private Permanent addStrider() {
        return harness.addToBattlefieldAndReturn(player1, new GoldfuryStrider());
    }
}
