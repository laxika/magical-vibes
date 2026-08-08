package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AethershieldArtificerTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Boosts the targeted artifact creature by +2/+2")
    void boostsTargetArtifactCreature() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, thopter.getId());
        harness.passBothPriorities();

        assertThat(thopter.getPowerModifier()).isEqualTo(2);
        assertThat(thopter.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Granted indestructible survives a destroy effect")
    void grantsIndestructible() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, thopter.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, thopter.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature an opponent controls")
    void cannotTargetOpponentArtifactCreature() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, thopter.getId());
        harness.passBothPriorities();
        assertThat(thopter.getPowerModifier()).isEqualTo(2);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thopter.getPowerModifier()).isEqualTo(0);
    }
}
