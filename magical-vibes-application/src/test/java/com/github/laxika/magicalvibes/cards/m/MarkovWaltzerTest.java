package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarkovWaltzerTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
    }

    @Test
    @DisplayName("Boosts two chosen creatures you control by +1/+0")
    void boostsTwoOwnCreatures() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears1.getId());
        harness.handlePermanentChosen(player1, bears2.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(bears1.getPowerModifier()).isEqualTo(1);
        assertThat(bears1.getToughnessModifier()).isEqualTo(0);
        assertThat(bears2.getPowerModifier()).isEqualTo(1);
        assertThat(bears2.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can stop after a single target")
    void boostsSingleTarget() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, player1.getId()); // choose self to stop
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enemy.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new MarkovWaltzer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(bears.getPowerModifier()).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
    }
}
