package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetLandWhileSourceOnBattlefieldEffect;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AwakenerDruidTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Awakener Druid has ETB effect targeting a permanent")
    void hasCorrectEffect() {
        AwakenerDruid card = new AwakenerDruid();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(AnimateTargetLandWhileSourceOnBattlefieldEffect.class);

        AnimateTargetLandWhileSourceOnBattlefieldEffect effect =
                (AnimateTargetLandWhileSourceOnBattlefieldEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.power()).isEqualTo(4);
        assertThat(effect.toughness()).isEqualTo(5);
        assertThat(effect.color()).isEqualTo(CardColor.GREEN);
        assertThat(effect.grantedSubtypes()).containsExactly(CardSubtype.TREEFOLK);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Awakener Druid puts it on the stack with Forest target")
    void castingPutsOnStackWithTarget() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castCreature(player1, 0, 0, forestId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Awakener Druid");
        assertThat(entry.getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("ETB trigger appears on stack after creature resolves")
    void etbTriggerAppearsOnStack() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castCreature(player1, 0, 0, forestId);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Awakener Druid"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("ETB resolves and animates target Forest into 4/5 green Treefolk creature")
    void etbAnimatesForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castCreature(player1, 0, 0, forestId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();

        assertThat(forest.isPermanentlyAnimated()).isTrue();
        assertThat(forest.getPermanentAnimatedPower()).isEqualTo(4);
        assertThat(forest.getPermanentAnimatedToughness()).isEqualTo(5);
        assertThat(forest.getEffectivePower()).isEqualTo(4);
        assertThat(forest.getEffectiveToughness()).isEqualTo(5);
        assertThat(forest.getGrantedSubtypes()).contains(CardSubtype.TREEFOLK);
        assertThat(forest.getGrantedColors()).contains(CardColor.GREEN);

        // It's still a land
        assertThat(forest.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(forest.getCard().getSubtypes()).contains(CardSubtype.FOREST);

        // It's a creature now
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        // Source-linked animation is tracked
        assertThat(gd.sourceLinkedAnimations).containsKey(forestId);
    }

    // ===== Cleanup when source leaves =====

    @Test
    @DisplayName("Forest reverts when Awakener Druid is destroyed")
    void forestRevertsWhenDruidDestroyed() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castCreature(player1, 0, 0, forestId);

        // Resolve creature spell + ETB
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Find the Druid permanent and destroy it
        Permanent druid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Awakener Druid"))
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().tryDestroyPermanent(gd, druid);

        // Forest should revert
        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();

        assertThat(forest.isPermanentlyAnimated()).isFalse();
        assertThat(forest.getGrantedSubtypes()).isEmpty();
        assertThat(forest.getGrantedColors()).isEmpty();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gd.sourceLinkedAnimations).isEmpty();
    }

    @Test
    @DisplayName("Forest reverts when Awakener Druid is bounced")
    void forestRevertsWhenDruidBounced() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castCreature(player1, 0, 0, forestId);

        // Resolve creature spell + ETB
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Bounce the Druid to hand
        Permanent druid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Awakener Druid"))
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToHand(gd, druid);

        // Forest should revert
        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();

        assertThat(forest.isPermanentlyAnimated()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gd.sourceLinkedAnimations).isEmpty();
    }

    // ===== Target fizzles =====

    @Test
    @DisplayName("ETB fizzles if target Forest is removed before resolution")
    void etbFizzlesIfForestRemoved() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castCreature(player1, 0, 0, forestId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove the Forest before ETB resolves
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.sourceLinkedAnimations).isEmpty();
    }

    @Test
    @DisplayName("ETB has no effect if Awakener Druid leaves before ETB resolves")
    void etbNoEffectIfDruidLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castCreature(player1, 0, 0, forestId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove the Druid before ETB resolves
        GameData gd = harness.getGameData();
        Permanent druid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Awakener Druid"))
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, druid);

        // Resolve ETB → no effect
        harness.passBothPriorities();

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();

        assertThat(forest.isPermanentlyAnimated()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gd.sourceLinkedAnimations).isEmpty();
    }

    // ===== Can cast without target =====

    @Test
    @DisplayName("Can cast without target when no Forests on battlefield")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Awakener Druid");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Awakener Druid"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Multiple Druids =====

    @Test
    @DisplayName("Two Awakener Druids animating different Forests - removing one only reverts one")
    void twoDruidsAnimateDifferentForests() {
        Forest forest1Card = new Forest();
        Forest forest2Card = new Forest();
        harness.addToBattlefield(player1, forest1Card);
        harness.addToBattlefield(player1, forest2Card);

        // Cast first Druid targeting first Forest
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        UUID forest1Id = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(forest1Card.getId()))
                .findFirst().orElseThrow().getId();
        harness.castCreature(player1, 0, 0, forest1Id);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        // Cast second Druid targeting second Forest
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        UUID forest2Id = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(forest2Card.getId()))
                .findFirst().orElseThrow().getId();
        harness.castCreature(player1, 0, 0, forest2Id);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.sourceLinkedAnimations).hasSize(2);

        // Destroy the first Druid
        List<Permanent> druids = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Awakener Druid"))
                .toList();
        harness.getPermanentRemovalService().tryDestroyPermanent(gd, druids.getFirst());

        // First Forest should revert, second should remain animated
        Permanent forest1 = gqs.findPermanentById(gd, forest1Id);
        Permanent forest2 = gqs.findPermanentById(gd, forest2Id);

        assertThat(forest1.isPermanentlyAnimated()).isFalse();
        assertThat(forest2.isPermanentlyAnimated()).isTrue();
        assertThat(forest2.getEffectivePower()).isEqualTo(4);
        assertThat(forest2.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.sourceLinkedAnimations).hasSize(1);
    }
}
