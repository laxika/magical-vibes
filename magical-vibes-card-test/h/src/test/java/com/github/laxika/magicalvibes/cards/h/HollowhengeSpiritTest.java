package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HollowhengeSpirit.class, SanctuaryCat.class})
class HollowhengeSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("ETB removes target attacking creature from combat")
    void etbRemovesAttacker() {
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, attacker.getId());

        // Resolve creature spell -> ETB triggers
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
    }

    @Test
    @DisplayName("ETB removes target blocking creature from combat")
    void etbRemovesBlocker() {
        Permanent blocker = addBlocker(player2);
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, blocker.getId());

        harness.passBothPriorities(); // resolve creature -> ETB triggers
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getBlockingTargetIds()).isEmpty();
    }

    @Test
    @DisplayName("ETB triggered ability goes on stack targeting the attacker")
    void etbGoesOnStackWithTarget() {
        Permanent attacker = addAttacker(player2);
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, attacker.getId());
        harness.passBothPriorities(); // resolve creature spell

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Hollowhenge Spirit");
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Hollowhenge Spirit");
        assertThat(trigger.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new SanctuaryCat());
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Sanctuary Cat");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

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
    @DisplayName("ETB is not put on the stack when no legal target exists")
    void etbIsNotPutOnStackWithoutLegalTargets() {
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Hollowhenge Spirit");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can flash in during the opponent's combat and remove an attacker")
    void canFlashInDuringOpponentsCombat() {
        Permanent attacker = addAttacker(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hollowhenge Spirit");
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
    }

    @Test
    @DisplayName("Can remove its controller's own attacking creature")
    void canRemoveOwnAttacker() {
        Permanent attacker = addAttacker(player1);
        attacker.tap();
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
        assertThat(attacker.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Sanctuary Cat");
    }

    @Test
    @DisplayName("Removing the sole blocker leaves the attacker blocked")
    void removingSoleBlockerLeavesAttackerBlocked() {
        Permanent attacker = addAttacker(player2);
        Permanent blocker = addCreatureReady(player1, new SanctuaryCat());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, blocker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getBlockingTargetIds()).isEmpty();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    @DisplayName("A target that leaves combat before resolution remains on the battlefield")
    void targetLeavesCombatBeforeResolution() {
        Permanent attacker = addAttacker(player2);
        attacker.tap();
        harness.setHand(player1, List.of(new HollowhengeSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Hollowhenge Spirit");
        harness.assertOnBattlefield(player2, "Sanctuary Cat");
        assertThat(attacker.isTapped()).isTrue();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent attacker = addCreatureReady(owner, new SanctuaryCat());
        attacker.setAttacking(true);
        attacker.setAttackTarget(owner.equals(player1) ? player2.getId() : player1.getId());
        return attacker;
    }

    private Permanent addBlocker(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent attacker = addAttacker(owner.equals(player1) ? player2 : player1);
        Permanent blocker = addCreatureReady(owner, new SanctuaryCat());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        return blocker;
    }
}
