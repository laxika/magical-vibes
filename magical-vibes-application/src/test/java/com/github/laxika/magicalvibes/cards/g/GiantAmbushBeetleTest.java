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

    // ===== Helpers =====

    /** Casts the beetle and resolves it onto the battlefield; the ETB "may" trigger is left on the stack. */
    private Permanent castBeetle() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiantAmbushBeetle()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB may on stack

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Giant Ambush Beetle"))
                .findFirst().orElseThrow();
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Accepting the ETB and choosing a creature makes it block the beetle")
    void acceptingSetsMustBlock() {
        Permanent target = addCreature(player2);
        Permanent beetle = castBeetle();

        harness.passBothPriorities();              // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> target choice
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.getMustBlockIds()).contains(beetle.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB imposes no block requirement")
    void decliningImposesNoRequirement() {
        Permanent target = addCreature(player2);
        castBeetle();

        harness.passBothPriorities();               // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(target.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can force one of the controller's own creatures to block the beetle")
    void canTargetOwnCreature() {
        Permanent ownCreature = addCreature(player1);
        Permanent beetle = castBeetle();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(ownCreature.getMustBlockIds()).contains(beetle.getId());
    }

    // ===== Combat enforcement =====

    @Test
    @DisplayName("Chosen creature must be declared as a blocker when the beetle attacks")
    void chosenCreatureMustBlockBeetle() {
        Permanent target = addCreature(player2);
        Permanent beetle = castBeetle();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

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

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        beetle.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
