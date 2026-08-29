package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeldfastEngineerTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Boosts a target artifact creature you control by +2/+0")
    void boostsTargetArtifactCreature() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, familiar.getId());
        harness.passBothPriorities();

        assertThat(familiar.getPowerModifier()).isEqualTo(2);
        assertThat(familiar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target an artifact creature controlled by an opponent")
    void cannotTargetOpponentArtifactCreature() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());
        Permanent familiar = harness.addToBattlefieldAndReturn(player2, new JhoirasFamiliar());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, familiar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature")
    void cannotTargetNonartifactCreature() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new WeldfastEngineer());
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new JhoirasFamiliar());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, familiar.getId());
        harness.passBothPriorities();
        assertThat(familiar.getPowerModifier()).isEqualTo(2);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(familiar.getPowerModifier()).isEqualTo(0);
    }
}
