package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManifoldKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps another target artifact")
    void untapsAnotherTargetArtifact() {
        addReadyKey(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        artifact.tap();
        addMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself with the untap ability")
    void cannotTargetItselfWithUntapAbility() {
        Permanent key = addReadyKey(player1);
        addMana(ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, key.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(key.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-artifact with the untap ability")
    void cannotTargetNonArtifactWithUntapAbility() {
        addReadyKey(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMana(ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Makes a target creature unblockable until end of turn")
    void makesTargetCreatureUnblockableUntilEndOfTurn() {
        addReadyKey(player1);
        Permanent creature = addCreature(player2);
        addMana(ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable effect wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        addReadyKey(player1);
        Permanent creature = addCreature(player1);
        addMana(ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature with the unblockable ability")
    void cannotTargetNoncreatureWithUnblockableAbility() {
        addReadyKey(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMana(ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKey(Player player) {
        Permanent key = new Permanent(new ManifoldKey());
        key.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(key);
        return key;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }
}
