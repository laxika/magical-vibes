package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BleakCovenVampiresTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has metalcraft-conditional ETB drain effect")
    void hasMetalcraftEtbEffect() {
        BleakCovenVampires card = new BleakCovenVampires();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft =
                (MetalcraftConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(TargetPlayerLosesLifeAndControllerGainsLifeEffect.class);

        TargetPlayerLosesLifeAndControllerGainsLifeEffect drain =
                (TargetPlayerLosesLifeAndControllerGainsLifeEffect) metalcraft.wrapped();
        assertThat(drain.lifeLoss()).isEqualTo(4);
        assertThat(drain.lifeGain()).isEqualTo(4);
    }

    @Test
    @DisplayName("Card needs target (delegates from metalcraft wrapper)")
    void needsTarget() {
        BleakCovenVampires card = new BleakCovenVampires();
        assertThat(EffectResolution.needsTarget(card)).isTrue();
    }

    // ===== ETB with metalcraft met =====

    @Test
    @DisplayName("ETB triggers drain when metalcraft is met (3+ artifacts)")
    void etbTriggersWithMetalcraft() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bleak Coven Vampires");
    }

    @Test
    @DisplayName("ETB drain resolves: target loses 4 life, controller gains 4 life")
    void etbDrainsLifeWithMetalcraft() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("ETB drain works with non-default life totals")
    void etbDrainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Game log records life drain with metalcraft")
    void gameLogRecordsLifeChanges() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog).anyMatch(log -> log.contains("loses 4 life"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains 4 life"));
    }

    // ===== ETB without metalcraft =====

    @Test
    @DisplayName("ETB does NOT trigger without metalcraft (0 artifacts)")
    void etbDoesNotTriggerWithoutMetalcraft() {
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bleak Coven Vampires"));

        // Life totals unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does NOT trigger with only 2 artifacts")
    void etbDoesNotTriggerWithTwoArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger
        assertThat(gd.stack).isEmpty();

        // Life totals unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Metalcraft lost before resolution =====

    @Test
    @DisplayName("ETB does nothing if metalcraft is lost before resolution")
    void etbFizzlesWhenMetalcraftLost() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove artifacts before ETB resolves (simulating opponent destroying them)
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Spellbook"));

        harness.passBothPriorities(); // resolve ETB trigger — metalcraft no longer met

        // Life totals unchanged (ability does nothing)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("metalcraft ability does nothing"));
    }

    // ===== Creature enters battlefield regardless =====

    @Test
    @DisplayName("Creature enters battlefield even without metalcraft")
    void creatureEntersWithoutMetalcraft() {
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bleak Coven Vampires"));
    }

    @Test
    @DisplayName("Stack is empty after full resolution with metalcraft")
    void stackEmptyAfterResolution() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void setupMetalcraft() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
    }

    private void castBleakCovenVampires() {
        harness.setHand(player1, List.of(new BleakCovenVampires()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
    }
}
