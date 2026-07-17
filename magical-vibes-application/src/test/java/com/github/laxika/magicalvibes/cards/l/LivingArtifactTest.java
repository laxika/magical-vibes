package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LivingArtifactTest extends BaseCardTest {

    /** Puts a MindStone artifact + a Living Artifact aura attached to it on player1's battlefield. */
    private Permanent enchantArtifact() {
        Permanent artifact = new Permanent(new MindStone());
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        Permanent aura = new Permanent(new LivingArtifact());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    // ===== Whenever you're dealt damage, add vitality counters =====

    @Test
    @DisplayName("Damage to the controller adds that many vitality counters")
    void damageAddsVitalityCounters() {
        Permanent aura = enchantArtifact();
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities(); // Lightning Bolt resolves — 3 damage to player1
        harness.passBothPriorities(); // vitality-counter trigger resolves

        assertThat(aura.getCounterCount(CounterType.VITALITY)).isEqualTo(3);
    }

    // ===== Upkeep: you may remove a vitality counter to gain 1 life =====

    @Test
    @DisplayName("Upkeep: removing a vitality counter gains 1 life")
    void upkeepRemoveCounterGainsLife() {
        Permanent aura = enchantArtifact();
        aura.setCounterCount(CounterType.VITALITY, 2);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passBothPriorities(); // advance to upkeep, trigger queued
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, true);

        assertThat(aura.getCounterCount(CounterType.VITALITY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Upkeep: declining keeps the counter and gains no life")
    void upkeepDeclineKeepsCounter() {
        Permanent aura = enchantArtifact();
        aura.setCounterCount(CounterType.VITALITY, 2);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(aura.getCounterCount(CounterType.VITALITY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Upkeep: accepting with no vitality counters gains no life")
    void upkeepNoCountersGainsNoLife() {
        Permanent aura = enchantArtifact();
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(aura.getCounterCount(CounterType.VITALITY)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Enchant artifact targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a non-artifact")
    void cannotEnchantNonArtifact() {
        // A legal artifact target exists (so the Aura is playable), but we aim at a creature.
        Permanent artifact = new Permanent(new MindStone());
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new LivingArtifact()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
