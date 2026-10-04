package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneSalamander.class, TrainedArmodon.class})
class FlowstoneSalamanderTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: deals 1 damage to a creature blocking Flowstone Salamander")
    void damagesBlocker() {
        addCreatureReady(player1, new FlowstoneSalamander());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());

        blockSalamander();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for more damage")
    void damageStacksAcrossActivations() {
        addCreatureReady(player1, new FlowstoneSalamander());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());

        blockSalamander();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the chosen creature is damaged when multiple creatures block Flowstone Salamander")
    void damagesOnlyChosenBlocker() {
        addCreatureReady(player1, new FlowstoneSalamander());
        Permanent firstBlocker = addCreatureReady(player2, new TrainedArmodon());
        Permanent chosenBlocker = addCreatureReady(player2, new TrainedArmodon());

        blockSalamander(List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, chosenBlocker.getId());
        harness.passBothPriorities();

        assertThat(firstBlocker.getMarkedDamage()).isZero();
        assertThat(chosenBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that isn't blocking Flowstone Salamander")
    void cannotTargetNonBlocker() {
        addCreatureReady(player1, new FlowstoneSalamander());
        addCreatureReady(player2, new TrainedArmodon());
        Permanent bystander = addCreatureReady(player2, new TrainedArmodon());

        blockSalamander();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations can kill the blocker before combat damage")
    void lethalAbilityDamageKillsBlocker() {
        addCreatureReady(player1, new FlowstoneSalamander());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());

        blockSalamander();
        harness.addMana(player1, ManaColor.RED, 3);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, 0, null, blocker.getId());
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player2, "Trained Armodon");
        harness.assertInGraveyard(player2, "Trained Armodon");
        harness.assertOnBattlefield(player1, "Flowstone Salamander");
    }

    @Test
    @DisplayName("Cannot target a creature blocking a different attacker")
    void cannotTargetBlockerOfAnotherAttacker() {
        addCreatureReady(player1, new FlowstoneSalamander());
        addCreatureReady(player1, new TrainedArmodon());
        addCreatureReady(player2, new TrainedArmodon());
        Permanent otherBlocker = addCreatureReady(player2, new TrainedArmodon());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)
        ));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, otherBlocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature before it has been declared as a blocker")
    void cannotTargetBeforeBlockersAreDeclared() {
        addCreatureReady(player1, new FlowstoneSalamander());
        Permanent potentialBlocker = addCreatureReady(player2, new TrainedArmodon());

        declareAttackers(List.of(0));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, potentialBlocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Attacks with the Salamander and blocks it with player2's first creature. */
    private void blockSalamander() {
        blockSalamander(List.of(new BlockerAssignment(0, 0)));
    }

    private void blockSalamander(List<BlockerAssignment> blockerAssignments) {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, blockerAssignments);
    }
}
