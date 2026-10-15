package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.p.Pestermite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntDown.class, WoodlandChangeling.class, Pestermite.class})
class HuntDownTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving adds the blocked creature to the blocker's mustBlockIds")
    void resolvingAddsMustBlockRestriction() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));

        assertThat(blocker.getMustBlockIds()).contains(attacker.getId());
    }

    @Test
    @DisplayName("Blocker must block the chosen attacker (declaring no blockers fails)")
    void blockerMustBlockChosenAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));

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
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));

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
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent otherAttacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));

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
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));
        assertThat(blocker.getMustBlockIds()).contains(attacker.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(blocker.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("Spell resolves without imposing a requirement when the second target leaves")
    void noRequirementWhenSecondTargetRemoved() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hunt Down");
        assertThat(blocker.getMustBlockIds()).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("A tapped creature is not required to block")
    void tappedBlockerCannotBeForcedToBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));

        blocker.tap();
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A blocking requirement does not allow a ground creature to block flying")
    void cannotBlockFlyingAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Pestermite());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Both targets may be the same creature")
    void sameCreatureCanBeChosenForBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId(), creature.getId()));

        assertThat(creature.getMustBlockIds()).contains(creature.getId());
        harness.assertInGraveyard(player1, "Hunt Down");
    }

    @Test
    @DisplayName("Removing the first target does not impose a requirement on the second")
    void noRequirementWhenFirstTargetRemoved() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new HuntDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(blocker.getId(), attacker.getId()));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(attacker.getMustBlockIds()).isEmpty();
        harness.assertInGraveyard(player1, "Hunt Down");
    }
}
