package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SurgeNodeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB effect for entering with 6 charge counters")
    void hasEnterWithChargeCountersEffect() {
        SurgeNode card = new SurgeNode();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect = (EnterWithFixedChargeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(6);
    }

    @Test
    @DisplayName("Has activated ability: {1}, tap, remove charge counter to put charge counter on target artifact")
    void hasActivatedAbility() {
        SurgeNode card = new SurgeNode();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof PutCounterOnTargetPermanentEffect pct && pct.counterType() == CounterType.CHARGE);
        assertThat(card.getActivatedAbilities().getFirst().getTargetFilter()).isNotNull();
    }

    // ===== Entering the battlefield with charge counters =====

    @Test
    @DisplayName("Enters the battlefield with 6 charge counters")
    void entersWithSixChargeCounters() {
        harness.setHand(player1, List.of(new SurgeNode()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent node = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Surge Node"))
                .findFirst().orElseThrow();
        assertThat(node.getChargeCounters()).isEqualTo(6);
    }

    // ===== Activated ability: put charge counter on target artifact =====

    @Test
    @DisplayName("Activating ability removes a charge counter from Surge Node and puts one on target artifact")
    void activateRemovesCounterAndPutsOnTarget() {
        harness.addToBattlefield(player1, new SurgeNode());
        // Add another artifact as a target
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent surgeNode = gd.playerBattlefields.get(player1.getId()).get(0);
        surgeNode.setChargeCounters(6);
        Permanent targetArtifact = gd.playerBattlefields.get(player1.getId()).get(1);
        targetArtifact.setChargeCounters(0);

        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(surgeNode.getChargeCounters()).isEqualTo(5);
        assertThat(targetArtifact.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's artifact")
    void canTargetOpponentArtifact() {
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addToBattlefield(player2, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent surgeNode = gd.playerBattlefields.get(player1.getId()).get(0);
        surgeNode.setChargeCounters(6);
        Permanent opponentArtifact = gd.playerBattlefields.get(player2.getId()).get(0);
        opponentArtifact.setChargeCounters(0);

        harness.activateAbility(player1, 0, null, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(surgeNode.getChargeCounters()).isEqualTo(5);
        assertThat(opponentArtifact.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent surgeNode = gd.playerBattlefields.get(player1.getId()).get(0);
        surgeNode.setChargeCounters(0);
        Permanent targetArtifact = gd.playerBattlefields.get(player1.getId()).get(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addToBattlefield(player1, new SurgeNode());

        Permanent surgeNode = gd.playerBattlefields.get(player1.getId()).get(0);
        surgeNode.setChargeCounters(6);
        Permanent targetArtifact = gd.playerBattlefields.get(player1.getId()).get(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent surgeNode = gd.playerBattlefields.get(player1.getId()).get(0);
        surgeNode.setChargeCounters(6);
        surgeNode.tap();
        Permanent targetArtifact = gd.playerBattlefields.get(player1.getId()).get(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple activations deplete charge counters")
    void multipleActivationsDepleteCounters() {
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addToBattlefield(player1, new SurgeNode());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent surgeNode = gd.playerBattlefields.get(player1.getId()).get(0);
        surgeNode.setChargeCounters(6);
        Permanent targetArtifact = gd.playerBattlefields.get(player1.getId()).get(1);
        targetArtifact.setChargeCounters(0);

        // First activation
        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();
        surgeNode.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, targetArtifact.getId());
        harness.passBothPriorities();

        assertThat(surgeNode.getChargeCounters()).isEqualTo(4);
        assertThat(targetArtifact.getChargeCounters()).isEqualTo(2);
    }
}
