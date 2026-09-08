package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThermalNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability sacrifices an artifact and puts the ability on the stack")
    void sacrificesArtifact() {
        harness.addToBattlefield(player1, new ThermalNavigator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability grants flying to Thermal Navigator on resolution")
    void grantsFlyingOnResolution() {
        harness.addToBattlefield(player1, new ThermalNavigator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
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
