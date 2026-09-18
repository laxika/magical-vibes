package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.cards.b.BlindCreeper;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThermalNavigator.class, AvariceTotem.class, BlindCreeper.class})
class ThermalNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices an artifact and puts the ability on the stack")
    void sacrificesArtifact() {
        harness.addToBattlefield(player1, new ThermalNavigator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Avarice Totem");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot choose a non-artifact to pay the sacrifice cost")
    void cannotSacrificeNonArtifact() {
        harness.addToBattlefield(player1, new ThermalNavigator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player1, new BlindCreeper());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Invalid permanent:");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Avarice Totem");
        harness.assertOnBattlefield(player1, "Blind Creeper");
        assertThat(findPermanent(player1, "Thermal Navigator").hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability grants flying to Thermal Navigator on resolution")
    void grantsFlyingOnResolution() {
        harness.addToBattlefield(player1, new ThermalNavigator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        Permanent navigator = findPermanent(player1, "Thermal Navigator");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(navigator.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ThermalNavigator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        Permanent navigator = findPermanent(player1, "Thermal Navigator");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        assertThat(navigator.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(navigator.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Thermal Navigator may be sacrificed as the artifact cost")
    void maySacrificeSource() {
        harness.addToBattlefield(player1, new ThermalNavigator());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Thermal Navigator");
        harness.assertInGraveyard(player1, "Thermal Navigator");
    }
}
