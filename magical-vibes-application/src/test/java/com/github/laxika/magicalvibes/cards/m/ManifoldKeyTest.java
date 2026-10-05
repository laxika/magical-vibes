package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManifoldKey.class, Forest.class, GreenwoodSentinel.class})
class ManifoldKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps another target artifact")
    void untapsAnotherTargetArtifact() {
        addReadyKey(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManifoldKey());
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

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An artifact can use its tap ability on the turn it enters")
    void newlyEnteredKeyCanUntapAnotherKey() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ManifoldKey());
        target.tap();
        addMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(key.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(key.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untap ability still resolves after the Key leaves the battlefield")
    void untapResolvesWithoutSource() {
        Permanent key = addReadyKey(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManifoldKey());
        target.tap();
        addMana(ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(key);
        gd.playerGraveyards.get(player1.getId()).add(key.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Unblockable ability still resolves after the Key leaves the battlefield")
    void unblockableResolvesWithoutSource() {
        Permanent key = addReadyKey(player1);
        Permanent target = addCreature(player1);
        addMana(ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(key.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(key);
        gd.playerGraveyards.get(player1.getId()).add(key.getCard());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Untap ability requires one mana")
    void untapRequiresMana() {
        Permanent key = addReadyKey(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManifoldKey());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(key.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unblockable ability requires three mana")
    void unblockableRequiresThreeMana() {
        Permanent key = addReadyKey(player1);
        Permanent target = addCreature(player1);
        addMana(ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(key.isTapped()).isFalse();
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Key cannot activate either ability")
    void tappedKeyCannotActivateEitherAbility() {
        Permanent key = addReadyKey(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManifoldKey());
        Permanent creature = addCreature(player1);
        key.tap();
        addMana(ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyKey(Player player) {
        Permanent key = harness.addToBattlefieldAndReturn(player, new ManifoldKey());
        key.setSummoningSick(false);
        return key;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GreenwoodSentinel());
        creature.setSummoningSick(false);
        return creature;
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }
}
