package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleverConjurer.class, Forest.class})
class CleverConjurerTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a target permanent not named Clever Conjurer")
    void untapsTargetPermanent() {
        Permanent conjurer = addReadyConjurer();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(conjurer.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent named Clever Conjurer")
    void cannotTargetCleverConjurer() {
        addReadyConjurer();
        Permanent otherConjurer = addCreatureReady(player1, new CleverConjurer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherConjurer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void requiresSorcerySpeed() {
        Permanent conjurer = addReadyConjurer();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(conjurer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        Permanent conjurer = addReadyConjurer();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, conjurer.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(conjurer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target an untapped permanent you control")
    void canTargetUntappedPermanent() {
        Permanent conjurer = addReadyConjurer();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(conjurer.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent conjurer = harness.addToBattlefieldAndReturn(player1, new CleverConjurer());
        conjurer.setSummoningSick(true);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(conjurer.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the tap cost while already tapped")
    void cannotActivateWhileTapped() {
        Permanent conjurer = addReadyConjurer();
        conjurer.tap();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during another player's main phase")
    void cannotActivateOnOpponentsTurn() {
        Permanent conjurer = addReadyConjurer();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(conjurer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate in response to another ability")
    void requiresEmptyStack() {
        addReadyConjurer();
        Permanent secondConjurer = addReadyConjurer();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.activateAbility(player1, 0, null, forest.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(secondConjurer.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(forest.isTapped()).isFalse();
    }

    private Permanent addReadyConjurer() {
        return addCreatureReady(player1, new CleverConjurer());
    }
}
