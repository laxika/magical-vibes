package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheHive;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManicVandalTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Manic Vandal has correct card properties")
    void hasCorrectProperties() {
        ManicVandal card = new ManicVandal();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Manic Vandal puts it on the stack with target")
    void castingPutsItOnStackWithTarget() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "The Hive");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Manic Vandal");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Manic Vandal enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "The Hive");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Manic Vandal"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Manic Vandal");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "The Hive");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("The Hive"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("The Hive"));
    }

    @Test
    @DisplayName("Can destroy own artifact with ETB")
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new TheHive());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player1, "The Hive");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("The Hive"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("The Hive"));
    }

    @Test
    @DisplayName("ETB fizzles if target artifact is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "The Hive");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Target restriction =====

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no artifacts on battlefield")
    void canCastWithoutTargetWhenNoArtifacts() {
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Manic Vandal");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature should be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Manic Vandal"));
        // No triggered ability on stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new TheHive());
        harness.setHand(player1, List.of(new ManicVandal()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "The Hive");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
