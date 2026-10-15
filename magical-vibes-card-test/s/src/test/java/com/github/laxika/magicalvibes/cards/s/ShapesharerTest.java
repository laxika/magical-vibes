package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
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

@CardUsed({Shapesharer.class, GoldmeadowStalwart.class, SecludedGlen.class, AvianChangeling.class})
class ShapesharerTest extends BaseCardTest {

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

    @Test
    @DisplayName("Copy survives the controller's own end-of-turn cleanup")
    void copySurvivesOwnEndOfTurn() {
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        Permanent target = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(shapesharer.getId(), target.getId()));
        harness.passBothPriorities();
        assertThat(shapesharer.getCard().getName()).isEqualTo("Goldmeadow Stalwart");

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

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

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(shapesharer.getCard().getName()).isEqualTo("Shapesharer");
    }

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

    @Test
    @DisplayName("The same creature may be chosen for both target occurrences")
    void mayCopyItself() {
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(shapesharer.getId(), shapesharer.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(shapesharer.getCard().getName()).isEqualTo("Shapesharer");
    }

    @Test
    @DisplayName("An older copy remains after a newer copy expires first")
    void earlierCopyResumesWhenNewerCopyExpires() {
        addCreatureReady(player1, new Shapesharer());
        Permanent recipient = addCreatureReady(player1, new Shapesharer());
        Permanent avian = addCreatureReady(player1, new AvianChangeling());
        Permanent stalwart = addCreatureReady(player1, new GoldmeadowStalwart());
        addCreatureReady(player2, new Shapesharer());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(recipient.getId(), avian.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithMultiTargets(player2, 0, 0,
                List.of(recipient.getId(), stalwart.getId()));
        harness.passBothPriorities();
        assertThat(recipient.getCard().getName()).isEqualTo("Goldmeadow Stalwart");

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(recipient.getCard().getName()).isEqualTo("Avian Changeling");

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(recipient.getCard().getName()).isEqualTo("Shapesharer");
    }

    @Test
    @DisplayName("Copying an opponent's Shapeshifter expires on the ability controller's turn")
    void opponentCopyUsesAbilityControllersTurn() {
        addCreatureReady(player1, new Shapesharer());
        Permanent recipient = addCreatureReady(player2, new Shapesharer());
        Permanent creature = addCreatureReady(player2, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(recipient.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(recipient.getCard().getName()).isEqualTo("Goldmeadow Stalwart");

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(recipient.getCard().getName()).isEqualTo("Shapesharer");
    }

    @Test
    @DisplayName("A pending activation does nothing if its first target loses Shapeshifter")
    void doesNothingWhenFirstTargetLosesShapeshifter() {
        addCreatureReady(player1, new Shapesharer());
        Permanent recipient = addCreatureReady(player1, new Shapesharer());
        Permanent avian = addCreatureReady(player1, new AvianChangeling());
        Permanent stalwart = addCreatureReady(player1, new GoldmeadowStalwart());
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(recipient.getId(), avian.getId()));
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(recipient.getId(), stalwart.getId()));
        harness.passBothPriorities();
        assertThat(recipient.getCard().getName()).isEqualTo("Goldmeadow Stalwart");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(recipient.getCard().getName()).isEqualTo("Goldmeadow Stalwart");
    }
}
