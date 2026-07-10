package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishBranchbenderTest extends BaseCardTest {

    // ===== Activating =====

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

    // ===== Resolving =====

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
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("X equals the number of Elves the controller controls")
    void xScalesWithElfCount() {
        // Branchbender + two Llanowar Elves = 3 Elves → 3/3.
        addReadyBranchbender(player1);
        Permanent forest = addForest(player1);
        addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new LlanowarElves());

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

        // Simulate end-of-turn cleanup.
        forest.resetModifiers();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getTransientSubtypes()).doesNotContain(CardSubtype.TREEFOLK);
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a permanent that is not a Forest")
    void cannotTargetNonForest() {
        addReadyBranchbender(player1);
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyBranchbender(Player player) {
        Permanent perm = new Permanent(new ElvishBranchbender());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addForest(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
