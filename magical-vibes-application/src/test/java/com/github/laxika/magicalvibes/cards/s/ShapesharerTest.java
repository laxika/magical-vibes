package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShapesharerTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack with both targets")
    void activatingAbilityPutsOnStack() {
        Permanent shapesharer = addReady(player1, new Shapesharer());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), bears.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(shapesharer.getId(), bears.getId());
    }

    // ===== Copy resolution =====

    @Test
    @DisplayName("Resolving makes the target Shapeshifter a copy of the target creature")
    void becomesCopyOnResolution() {
        Permanent shapesharer = addReady(player1, new Shapesharer());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(shapesharer.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(shapesharer.getCard().getPower()).isEqualTo(2);
        assertThat(shapesharer.getCard().getToughness()).isEqualTo(2);
    }

    // ===== Until-your-next-turn duration =====

    @Test
    @DisplayName("Copy survives the controller's own end-of-turn cleanup")
    void copySurvivesOwnEndOfTurn() {
        Permanent shapesharer = addReady(player1, new Shapesharer());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), bears.getId()));
        harness.passBothPriorities();
        assertThat(shapesharer.getCard().getName()).isEqualTo("Grizzly Bears");

        // End player1's own turn -> player2's turn. Copy must NOT revert yet.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shapesharer.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Copy reverts at the beginning of the controller's next turn")
    void copyRevertsAtControllersNextTurn() {
        Permanent shapesharer = addReady(player1, new Shapesharer());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), bears.getId()));
        harness.passBothPriorities();
        assertThat(shapesharer.getCard().getName()).isEqualTo("Grizzly Bears");

        // End player2's turn -> player1's next turn, which reverts the copy.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shapesharer.getCard().getName()).isEqualTo("Shapesharer");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if the creature target leaves before resolution")
    void fizzlesIfCreatureLeaves() {
        Permanent shapesharer = addReady(player1, new Shapesharer());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), bears.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(shapesharer.getCard().getName()).isEqualTo("Shapesharer");
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("First target must be a Shapeshifter")
    void firstTargetMustBeShapeshifter() {
        addReady(player1, new Shapesharer());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent bears2 = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bears.getId(), bears2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
