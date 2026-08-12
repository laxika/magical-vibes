package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurntimberBasiliskTest extends BaseCardTest {

    private Permanent triggerLandfall(Permanent basilisk, Player targetController) {
        Permanent target = harness.addToBattlefieldAndReturn(targetController, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        return target;
    }

    @Test
    @DisplayName("Landfall can make a target creature block Turntimber Basilisk")
    void landfallSetsMustBlock() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new TurntimberBasilisk());

        Permanent target = triggerLandfall(basilisk, player2);

        assertThat(target.getMustBlockIds()).containsExactly(basilisk.getId());
    }

    @Test
    @DisplayName("Declining landfall imposes no block requirement")
    void decliningLandfallImposesNoRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TurntimberBasilisk());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Turntimber Basilisk")
    void opponentLandDoesNotTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TurntimberBasilisk());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(target.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The chosen creature must block Turntimber Basilisk when it attacks")
    void chosenCreatureMustBlockBasilisk() {
        Permanent basilisk = harness.addToBattlefieldAndReturn(player1, new TurntimberBasilisk());
        Permanent target = triggerLandfall(basilisk, player2);

        basilisk.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
