package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.j.JanglingAutomaton;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Touchstone.class, JanglingAutomaton.class, BenalishInfantry.class})
class TouchstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target artifact an opponent controls")
    void tapsTargetArtifactOpponentControls() {
        Permanent touchstone = harness.addToBattlefieldAndReturn(player1, new Touchstone());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JanglingAutomaton());

        harness.activateAbility(player1, 0, null, artifact.getId());

        assertThat(touchstone.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(touchstone.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact you control")
    void cannotTargetArtifactYouControl() {
        harness.addToBattlefield(player1, new Touchstone());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new JanglingAutomaton());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you don't control");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        harness.addToBattlefield(player1, new Touchstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you don't control");
    }

    @Test
    @DisplayName("Requires a target artifact")
    void requiresTargetArtifact() {
        Permanent touchstone = harness.addToBattlefieldAndReturn(player1, new Touchstone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ability requires a target");
        assertThat(touchstone.isTapped()).isFalse();
    }
}
