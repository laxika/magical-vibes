package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TouchstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target artifact an opponent controls")
    void tapsTargetArtifactOpponentControls() {
        Permanent touchstone = harness.addToBattlefieldAndReturn(player1, new Touchstone());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(touchstone.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact you control")
    void cannotTargetArtifactYouControl() {
        harness.addToBattlefield(player1, new Touchstone());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you don't control");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        harness.addToBattlefield(player1, new Touchstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you don't control");
    }
}
