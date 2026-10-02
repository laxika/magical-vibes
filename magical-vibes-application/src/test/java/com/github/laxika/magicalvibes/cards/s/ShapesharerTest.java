package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shapesharer.class, GoldmeadowStalwart.class, SecludedGlen.class})
class ShapesharerTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack with both targets")
    void activatingAbilityPutsOnStack() {
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        Permanent target = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), target.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(shapesharer.getId(), target.getId());
    }

    // ===== Copy resolution =====

    @Test
    @DisplayName("Resolving makes the target Shapeshifter a copy of the target creature")
    void becomesCopyOnResolution() {
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        Permanent target = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(shapesharer.getCard().getName()).isEqualTo("Goldmeadow Stalwart");
        assertThat(shapesharer.getCard().getPower()).isEqualTo(2);
        assertThat(shapesharer.getCard().getToughness()).isEqualTo(2);
    }

    // ===== Until-your-next-turn duration =====

    @Test
    @DisplayName("Copy survives the controller's own end-of-turn cleanup")
    void copySurvivesOwnEndOfTurn() {
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        Permanent target = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), target.getId()));
        harness.passBothPriorities();
        assertThat(shapesharer.getCard().getName()).isEqualTo("Goldmeadow Stalwart");

        // End player1's own turn -> player2's turn. Copy must NOT revert yet.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shapesharer.getCard().getName()).isEqualTo("Goldmeadow Stalwart");
    }

    @Test
    @DisplayName("Copy reverts at the beginning of the controller's next turn")
    void copyRevertsAtControllersNextTurn() {
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        Permanent target = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), target.getId()));
        harness.passBothPriorities();
        assertThat(shapesharer.getCard().getName()).isEqualTo("Goldmeadow Stalwart");

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
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        Permanent target = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(shapesharer.getCard().getName()).isEqualTo("Shapesharer");
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("First target must be a Shapeshifter")
    void firstTargetMustBeShapeshifter() {
        addCreatureReady(player1, new Shapesharer());
        Permanent target = addCreatureReady(player1, new GoldmeadowStalwart());
        Permanent target2 = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId(), target2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second target must be a creature")
    void secondTargetMustBeCreature() {
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new SecludedGlen());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0,
                        List.of(shapesharer.getId(), noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
