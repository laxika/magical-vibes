package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
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

class GiantAmbushBeetleTest extends BaseCardTest {

    private Permanent castBeetle() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiantAmbushBeetle()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        return findPermanent(player1, "Giant Ambush Beetle");
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    @Test
    @DisplayName("Accepting the ETB and choosing a creature makes it block the beetle")
    void acceptingSetsMustBlock() {
        Permanent target = addCreature(player2);
        Permanent beetle = castBeetle();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMustBlockIds()).contains(beetle.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB imposes no block requirement")
    void decliningImposesNoRequirement() {
        Permanent target = addCreature(player2);
        castBeetle();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can force one of the controller's own creatures to block the beetle")
    void canTargetOwnCreature() {
        Permanent ownCreature = addCreature(player1);
        Permanent beetle = castBeetle();

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.getMustBlockIds()).contains(beetle.getId());
    }

    @Test
    @DisplayName("Chosen creature must be declared as a blocker when the beetle attacks")
    void chosenCreatureMustBlockBeetle() {
        Permanent target = addCreature(player2);
        Permanent beetle = castBeetle();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        beetle.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Chosen creature satisfies the requirement by blocking the beetle")
    void chosenCreatureCanBlockBeetle() {
        Permanent target = addCreature(player2);
        Permanent beetle = castBeetle();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        beetle.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
