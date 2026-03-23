package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImperialAerosaurTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Imperial Aerosaur has correct ETB effects")
    void hasCorrectProperties() {
        ImperialAerosaur card = new ImperialAerosaur();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0))
                .isInstanceOf(BoostTargetCreatureEffect.class);
        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0);
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1))
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1);
        assertThat(grant.keywords()).containsExactly(Keyword.FLYING);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting with a target puts it on the stack")
    void castingWithTargetPutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Imperial Aerosaur");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        // Resolve creature spell — enters battlefield, ETB triggers
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Imperial Aerosaur"));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Imperial Aerosaur");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    // ===== ETB gives +1/+1 and flying =====

    @Test
    @DisplayName("ETB resolves and gives target creature +1/+1 and flying")
    void etbBoostsAndGrantsFlying() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    // ===== Boost and flying wear off at end of turn =====

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void boostAndFlyingWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);

        // Advance to end step — modifiers and granted keywords reset
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    // ===== Cannot target self =====

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetSelf() {
        harness.addToBattlefield(player1, new ImperialAerosaur());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID aerosaurOnBattlefieldId = harness.getPermanentId(player1, "Imperial Aerosaur");

        // Casting a second Aerosaur targeting the first should work
        gs.playCard(gd, player1, 0, 0, aerosaurOnBattlefieldId, null);
        harness.passBothPriorities(); // Resolve creature

        // ETB should target the first Aerosaur, not the one that just entered
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(aerosaurOnBattlefieldId);
    }

    // ===== Cannot target opponent's creature =====

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID opponentBears = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, opponentBears, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    // ===== Can cast without a target =====

    @Test
    @DisplayName("Can cast without a target when no other creatures you control")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Imperial Aerosaur");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Imperial Aerosaur"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImperialAerosaur()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        // Resolve creature spell — ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(targetId));

        // Resolve ETB — fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
