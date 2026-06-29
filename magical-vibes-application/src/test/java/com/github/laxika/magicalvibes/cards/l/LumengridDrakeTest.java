package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LumengridDrakeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has metalcraft-conditional ETB bounce effect")
    void hasMetalcraftEtbEffect() {
        LumengridDrake card = new LumengridDrake();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft =
                (MetalcraftConditionalEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(ReturnTargetPermanentToHandEffect.class);
    }

    @Test
    @DisplayName("Card needs target (delegates from metalcraft wrapper)")
    void needsTarget() {
        LumengridDrake card = new LumengridDrake();
        assertThat(EffectResolution.needsTarget(card)).isTrue();
    }

    // ===== ETB with metalcraft met =====

    @Test
    @DisplayName("ETB triggers when metalcraft is met (3+ artifacts)")
    void etbTriggersWithMetalcraft() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lumengrid Drake");
    }

    @Test
    @DisplayName("ETB resolves: target creature is returned to owner's hand")
    void etbBouncesCreatureWithMetalcraft() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Drake enters the battlefield when metalcraft is met")
    void drakeEntersWithMetalcraft() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lumengrid Drake"));
    }

    @Test
    @DisplayName("Stack is empty after full resolution with metalcraft")
    void stackEmptyAfterResolution() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== ETB without metalcraft =====

    @Test
    @DisplayName("ETB does NOT trigger without metalcraft (0 artifacts)")
    void etbDoesNotTriggerWithoutMetalcraft() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Drake is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lumengrid Drake"));

        // Target creature was NOT bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB does NOT trigger with only 2 artifacts")
    void etbDoesNotTriggerWithTwoArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger
        assertThat(gd.stack).isEmpty();

        // Target creature was NOT bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Metalcraft lost before resolution =====

    @Test
    @DisplayName("ETB does nothing if metalcraft is lost before resolution")
    void etbFizzlesWhenMetalcraftLost() {
        setupMetalcraft();
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLumengridDrake();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove artifacts before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Spellbook"));

        harness.passBothPriorities(); // resolve ETB trigger — metalcraft no longer met

        // Target creature was NOT bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("metalcraft ability does nothing"));
    }

    // ===== Can bounce own creature =====

    @Test
    @DisplayName("Can bounce own creature with metalcraft")
    void canBounceOwnCreature() {
        setupMetalcraft();
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new LumengridDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private void setupMetalcraft() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
    }

    private void castLumengridDrake() {
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new LumengridDrake()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }
}
