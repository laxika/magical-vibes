package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudSprite.class, FreshVolunteers.class})
class CloudSpriteTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Cloud Sprite puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CloudSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Cloud Sprite onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CloudSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cloud Sprite");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new CloudSprite()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cloud Sprite enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new CloudSprite()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Cloud Sprite");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Blocking — can block creatures with flying =====

    @Test
    @DisplayName("Cloud Sprite can block a creature with flying")
    void canBlockFlyingCreature() {
        // Player2 has Cloud Sprite as potential blocker
        Permanent spritePerm = addCreatureReady(player2, new CloudSprite());

        // Player1 has a flying attacker
        Permanent atkPerm = addCreatureReady(player1, new CloudSprite());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        // Should not throw — Cloud Sprite can block flyers
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spritePerm.isBlocking()).isTrue();
    }

    // ===== Blocking — cannot block creatures without flying =====

    @Test
    @DisplayName("Cloud Sprite cannot be blocked by a creature without flying or reach")
    void flyingPreventsNonFlyingBlocker() {
        addCreatureReady(player2, new FreshVolunteers());

        Permanent atkPerm = addCreatureReady(player1, new CloudSprite());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Cloud Sprite cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        // Player2 has Cloud Sprite as potential blocker
        Permanent spritePerm = addCreatureReady(player2, new CloudSprite());

        // Player1 has a ground attacker
        Permanent atkPerm = addCreatureReady(player1, new FreshVolunteers());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    // ===== Combat — Cloud Sprite trades with another 1/1 flyer =====

    @Test
    @DisplayName("Cloud Sprite trades in combat with another 1/1 flyer")
    void tradesWithOneOneFlyer() {
        harness.setLife(player2, 20);

        // Player1 has Cloud Sprite as attacker
        Permanent atkPerm = addCreatureReady(player1, new CloudSprite());
        atkPerm.setAttacking(true);

        // Player2 has Cloud Sprite as blocker
        Permanent blockerPerm = addCreatureReady(player2, new CloudSprite());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        resolveCombat();

        // Both should be dead
        harness.assertInGraveyard(player1, "Cloud Sprite");
        harness.assertInGraveyard(player2, "Cloud Sprite");

        // No damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Cloud Sprite deals combat damage when unblocked =====

    @Test
    @DisplayName("Unblocked Cloud Sprite deals 1 damage to defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new CloudSprite());
        atkPerm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}

