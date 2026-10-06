package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PendrellDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimmeringBarrier.class, PendrellDrake.class, Songstitcher.class})
class ShimmeringBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ShimmeringBarrier()));
        harness.setLibrary(player1, List.of(new PendrellDrake()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shimmering Barrier");
        harness.assertInHand(player1, "Pendrell Drake");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cycling cannot be activated without two generic mana")
    void cyclingRequiresTwoGenericMana() {
        harness.setHand(player1, List.of(new ShimmeringBarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Shimmering Barrier");
        harness.assertNotInGraveyard(player1, "Shimmering Barrier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void defenderPreventsAttacking() {
        Permanent barrier = addCreatureReady(player1, new ShimmeringBarrier());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(barrier.isAttacking()).isFalse();
    }

    @Test
    void firstStrikeKillsAttackerBeforeItDealsDamage() {
        addCreatureReady(player1, new Songstitcher());
        Permanent barrier = addCreatureReady(player2, new ShimmeringBarrier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Songstitcher");
        harness.assertNotOnBattlefield(player1, "Songstitcher");
        harness.assertOnBattlefield(player2, "Shimmering Barrier");
        assertThat(barrier.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void cyclingDiscardsAsCostAndDrawsOnlyOnResolution() {
        harness.setHand(player1, List.of(new ShimmeringBarrier()));
        harness.setLibrary(player1, List.of(new PendrellDrake()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Shimmering Barrier");
        harness.assertNotInHand(player1, "Shimmering Barrier");
        harness.assertNotInHand(player1, "Pendrell Drake");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Pendrell Drake");
        harness.assertNotInHand(player2, "Pendrell Drake");
        assertThat(gd.stack).isEmpty();
    }
}
