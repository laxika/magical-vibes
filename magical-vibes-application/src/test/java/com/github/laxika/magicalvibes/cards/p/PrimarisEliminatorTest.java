package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({PrimarisEliminator.class, GrizzlyBears.class, HillGiant.class})
class PrimarisEliminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Executioner Round destroys target creature")
    void executionerRoundDestroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPrimarisEliminator(0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Primaris Eliminator");
    }

    @Test
    @DisplayName("Hyperfrag Round weakens creatures controlled by the target player")
    void hyperfragRoundWeakensTargetPlayersCreaturesUntilEndOfTurn() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castPrimarisEliminator(1, player2.getId());

        assertThat(targetCreature.getEffectivePower()).isEqualTo(1);
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(targetCreature.getEffectivePower()).isEqualTo(3);
        assertThat(targetCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Each mode only accepts its own target type")
    void eachModeOnlyAcceptsItsOwnTargetType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimarisEliminator()));
        addPrimarisEliminatorMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    private void castPrimarisEliminator(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PrimarisEliminator()));
        addPrimarisEliminatorMana();
        harness.castCreature(player1, 0, mode, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addPrimarisEliminatorMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
