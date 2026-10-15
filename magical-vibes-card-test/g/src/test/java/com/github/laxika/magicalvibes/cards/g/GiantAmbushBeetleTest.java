package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantAmbushBeetle.class, QasaliPridemage.class})
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
        return harness.addToBattlefieldAndReturn(player, new QasaliPridemage());
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
        prepareDeclareBlockers();

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
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("A tapped target is not required to block")
    void tappedTargetDoesNotHaveToBlock() {
        Permanent target = addCreature(player2);
        Permanent beetle = castBeetle();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        target.tap();
        beetle.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("The target need not block another attacker when the beetle does not attack")
    void nonattackingBeetleDoesNotForceOtherBlocks() {
        Permanent target = addCreature(player2);
        Permanent otherAttacker = addCreature(player1);
        castBeetle();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("The block requirement expires at the end of the turn")
    void blockRequirementExpiresAtEndOfTurn() {
        Permanent target = addCreature(player2);
        castBeetle();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMustBlockIds()).isNotEmpty();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("The ETB does not offer the optional effect if its target leaves before resolution")
    void removedTargetMakesAbilityFizzle() {
        Permanent target = addCreature(player2);
        castBeetle();

        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMustBlockIds()).isEmpty();
    }

    @Test
    @DisplayName("The beetle can target itself and attack on the turn it enters")
    void canTargetItselfAndAttackImmediately() {
        addCreature(player2);
        Permanent beetle = castBeetle();

        harness.handlePermanentChosen(player1, beetle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(beetle.isAttacking()).isTrue();
        gs.declareBlockers(gd, player2, List.of());
    }
}
