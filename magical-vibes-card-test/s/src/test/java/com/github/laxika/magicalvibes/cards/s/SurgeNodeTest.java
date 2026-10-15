package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgeNode.class, Forest.class})
class SurgeNodeTest extends BaseCardTest {

    @Test
    void canTargetItselfAndPaysCostsBeforeResolution() {
        Permanent node = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        node.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, node.getId());

        assertThat(node.isTapped()).isTrue();
        assertThat(node.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(node.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void cannotTargetNonartifactPermanent() {
        Permanent node = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        node.setCounterCount(CounterType.CHARGE, 6);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(node.isTapped()).isFalse();
        assertThat(node.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent node = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SurgeNode());
        node.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(node);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedTargetDoesNotRefundCostsOrAffectOtherArtifacts() {
        Permanent node = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SurgeNode());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SurgeNode());
        node.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(node.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(node.isTapped()).isTrue();
        assertThat(other.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters the battlefield with 6 charge counters")
    void entersWithSixChargeCounters() {
        harness.setHand(player1, List.of(new SurgeNode()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent node = findPermanent(player1, "Surge Node");
        assertThat(node.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Activating ability removes a charge counter from Surge Node and puts one on target artifact")
    void activateRemovesCounterAndPutsOnTarget() {
        Permanent surgeNode = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        surgeNode.setCounterCount(CounterType.CHARGE, 6);
        targetArtifact.setCounterCount(CounterType.CHARGE, 0);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(surgeNode.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(targetArtifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's artifact")
    void canTargetOpponentArtifact() {
        Permanent surgeNode = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        surgeNode.setCounterCount(CounterType.CHARGE, 6);
        opponentArtifact.setCounterCount(CounterType.CHARGE, 0);

        harness.activateAbility(player1, 0, null, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(surgeNode.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(opponentArtifact.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        Permanent surgeNode = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        surgeNode.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent surgeNode = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player1, new SurgeNode());

        surgeNode.setCounterCount(CounterType.CHARGE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent surgeNode = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        surgeNode.setCounterCount(CounterType.CHARGE, 6);
        surgeNode.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple activations deplete charge counters")
    void multipleActivationsDepleteCounters() {
        Permanent surgeNode = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        Permanent targetArtifact = harness.addToBattlefieldAndReturn(player1, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        surgeNode.setCounterCount(CounterType.CHARGE, 6);
        targetArtifact.setCounterCount(CounterType.CHARGE, 0);

        // First activation
        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();
        surgeNode.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(surgeNode.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(targetArtifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }
}
