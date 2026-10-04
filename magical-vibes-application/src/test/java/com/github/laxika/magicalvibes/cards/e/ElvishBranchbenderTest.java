package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishBranchbender.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class ElvishBranchbenderTest extends BaseCardTest {


    @Test
    @DisplayName("Activating ability puts it on the stack targeting the Forest")
    void activatingPutsOnStack() {
        addReadyBranchbender(player1);
        Permanent forest = addForest(player1);

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(forest.getId());
    }


    @Test
    @DisplayName("Resolving animates target Forest into a Treefolk creature that is still a land")
    void animatesForestIntoTreefolk() {
        // Only the Branchbender itself is an Elf → X = 1.
        addReadyBranchbender(player1);
        Permanent forest = addForest(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
        assertThat(forest.getTransientSubtypes()).contains(CardSubtype.TREEFOLK);
        // Still a land (types are additive).
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("X equals the number of Elves the controller controls")
    void xScalesWithElfCount() {
        // Branchbender + two Llanowar Elves = 3 Elves → 3/3.
        addReadyBranchbender(player1);
        Permanent forest = addForest(player1);
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        addReadyBranchbender(player1);
        Permanent forest = addForest(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getTransientSubtypes()).doesNotContain(CardSubtype.TREEFOLK);
    }


    @Test
    @DisplayName("Cannot target a permanent that is not a Forest")
    void cannotTargetNonForest() {
        addReadyBranchbender(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opposing Forest uses the ability controller's Elf count")
    void animatesOpposingForestUsingOwnElves() {
        Permanent branchbender = addReadyBranchbender(player1);
        addReadyBranchbender(player1);
        addReadyBranchbender(player2);
        Permanent forest = addForest(player2);

        harness.activateAbility(player1, 0, null, forest.getId());
        assertThat(branchbender.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.TREEFOLK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
    }

    @Test
    @DisplayName("Elf count is determined on resolution and stays fixed afterward")
    void elfCountIsFixedAtResolution() {
        addReadyBranchbender(player1);
        Permanent forest = addForest(player1);
        harness.activateAbility(player1, 0, null, forest.getId());
        Permanent secondElf = addReadyBranchbender(player1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).remove(secondElf);
        gd.playerGraveyards.get(player1.getId()).add(secondElf.getCard());
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
    }

    @Test
    @DisplayName("With no Elves remaining on resolution the Forest dies as a zero-toughness creature")
    void noElvesOnResolutionKillsForest() {
        Permanent branchbender = addReadyBranchbender(player1);
        Permanent forest = addForest(player1);
        harness.activateAbility(player1, 0, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(branchbender);
        gd.playerGraveyards.get(player1.getId()).add(branchbender.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An ability whose Forest target leaves the battlefield does not animate another Forest")
    void departedTargetDoesNotAnimateAnotherForest() {
        addReadyBranchbender(player1);
        Permanent forest = addForest(player1);
        Permanent otherForest = addForest(player1);
        harness.activateAbility(player1, 0, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerGraveyards.get(player1.getId()).add(forest.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, otherForest)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest.getCard());
    }

    @Test
    @DisplayName("A summoning-sick Branchbender cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ElvishBranchbender());
        Permanent forest = addForest(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Branchbender cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent branchbender = addReadyBranchbender(player1);
        branchbender.setTapped(true);
        Permanent forest = addForest(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }


    private Permanent addReadyBranchbender(Player player) {
        return addCreatureReady(player, new ElvishBranchbender());
    }

    private Permanent addForest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
