package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuntDownTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving adds the blocked creature to the blocker's mustBlockIds")
    void resolvingAddsMustBlockRestriction() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(attacker.getId());
    }

    @Test
    @DisplayName("Blocker must block the chosen attacker (declaring no blockers fails)")
    void blockerMustBlockChosenAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("Requirement is satisfied by blocking the chosen attacker")
    void requirementSatisfiedByBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("No requirement when the chosen attacker does not attack")
    void noRequirementWhenAttackerDoesNotAttack() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherAttacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));
        harness.passBothPriorities();

        // The Hunt Down attacker stays home; a different creature attacks instead.
        otherAttacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Must-block restriction resets at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));
        harness.passBothPriorities();
        assertThat(blocker.getMustBlockIds()).contains(attacker.getId());

        blocker.resetModifiers();

        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Spell fizzles and imposes no requirement when a target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getMustBlockIds()).doesNotContain(attacker.getId());
    }
}
