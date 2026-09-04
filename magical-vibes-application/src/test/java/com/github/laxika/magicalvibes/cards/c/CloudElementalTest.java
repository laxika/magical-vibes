package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CloudElemental.class, AirElemental.class, GrizzlyBears.class, CloudSprite.class, GiantSpider.class})
class CloudElementalTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Cloud Elemental puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CloudElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Cloud Elemental onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CloudElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cloud Elemental");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new CloudElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cloud Elemental enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new CloudElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Cloud Elemental");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Blocking — can block creatures with flying =====

    @Test
    @DisplayName("Cloud Elemental can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent elementalPerm = addCreatureReady(player2, new CloudElemental());

        Permanent atkPerm = addCreatureReady(player1, new AirElemental());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(elementalPerm.isBlocking()).isTrue();
    }

    // ===== Blocking — cannot block creatures without flying =====

    @Test
    @DisplayName("Cloud Elemental cannot be blocked by a creature without flying or reach")
    void flyingPreventsNonFlyingBlocker() {
        addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new CloudElemental());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Cloud Elemental cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new CloudElemental());

        Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Cloud Elemental cannot block a creature with reach but without flying")
    void cannotBlockReachCreatureWithoutFlying() {
        addCreatureReady(player2, new CloudElemental());

        Permanent atkPerm = addCreatureReady(player1, new GiantSpider());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    // ===== Combat =====

    @Test
    @DisplayName("Cloud Elemental survives blocking a 1/1 flyer and kills it")
    void survivesBlockingSmallFlyer() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new CloudSprite());
        atkPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new CloudElemental());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        resolveCombat();

        // Cloud Sprite should die (1 toughness vs 2 damage)
        harness.assertInGraveyard(player1, "Cloud Sprite");
        // Cloud Elemental should survive (3 toughness vs 1 damage)
        harness.assertOnBattlefield(player2, "Cloud Elemental");
        // No damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Cloud Elemental deals 2 damage to defending player")
    void dealsTwoDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new CloudElemental());
        atkPerm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}

