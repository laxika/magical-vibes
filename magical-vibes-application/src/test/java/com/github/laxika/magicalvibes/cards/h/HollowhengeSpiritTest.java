package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HollowhengeSpiritTest extends BaseCardTest {

    // ===== Removing an attacker =====

    @Test
    @DisplayName("ETB removes target attacking creature from combat")
    void etbRemovesAttacker() {
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, attacker.getId(), null);

        // Resolve creature spell -> ETB triggers
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
    }

    // ===== Removing a blocker =====

    @Test
    @DisplayName("ETB removes target blocking creature from combat")
    void etbRemovesBlocker() {
        Permanent blocker = addBlocker(player2);
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, blocker.getId(), null);

        harness.passBothPriorities(); // resolve creature -> ETB triggers
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getBlockingTargetIds()).isEmpty();
    }

    // ===== ETB goes on stack with target =====

    @Test
    @DisplayName("ETB triggered ability goes on stack targeting the attacker")
    void etbGoesOnStackWithTarget() {
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, attacker.getId(), null);
        harness.passBothPriorities(); // resolve creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hollowhenge Spirit"));
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Hollowhenge Spirit");
        assertThat(trigger.getTargetId()).isEqualTo(attacker.getId());
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no creature is in combat")
    void canCastWithoutTargetWhenNoCombatCreatures() {
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hollowhenge Spirit");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hollowhenge Spirit"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        harness.addToBattlefield(owner, new GrizzlyBears());
        Permanent attacker = harness.getGameData().playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(com.github.laxika.magicalvibes.model.Player owner) {
        harness.addToBattlefield(owner, new GrizzlyBears());
        Permanent blocker = harness.getGameData().playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
