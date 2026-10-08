package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfGlare.class, ElvishLookout.class, Humility.class, MetathranSoldier.class})
class WallOfGlareTest extends BaseCardTest {

    @Test
    @DisplayName("Wall of Glare can block any number of creatures")
    void canBlockAnyNumberOfCreatures() {
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        for (int i = 0; i < 3; i++) {
            Permanent attacker = addCreatureReady(player1, new ElvishLookout());
            attacker.setAttacking(true);
        }

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)));

        assertThat(wall.isBlocking()).isTrue();
        assertThat(wall.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @CardUsed(Humility.class)
    @DisplayName("Wall of Glare loses its additional-block ability when creatures lose all abilities")
    void losesAdditionalBlockAbilityAfterHumility() {
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        for (int i = 0; i < 2; i++) {
            Permanent attacker = addCreatureReady(player1, new ElvishLookout());
            attacker.setAttacking(true);
        }
        harness.addToBattlefield(player1, new Humility());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned too many times");
        assertThat(wall.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Wall of Glare takes combat damage from every creature it blocks")
    void takesDamageFromEveryBlockedCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new ElvishLookout());
        }
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)));
        resolveCombat();

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Wall of Glare");
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Elvish Lookout")).isEqualTo(3);
    }

    @Test
    @DisplayName("Wall of Glare may block only some attackers")
    void mayLeaveAnAttackerUnblocked() {
        addCreatureReady(player2, new WallOfGlare());
        addCreatureReady(player1, new ElvishLookout());
        addCreatureReady(player1, new ElvishLookout());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Wall of Glare");
    }

    @Test
    @DisplayName("Unlimited blocking does not allow blocking an unblockable creature")
    void cannotBlockMetathranSoldier() {
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        addCreatureReady(player1, new ElvishLookout()).setAttacking(true);
        addCreatureReady(player1, new MetathranSoldier()).setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A tapped Wall of Glare cannot block")
    void cannotBlockWhileTapped() {
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        wall.tap();
        addCreatureReady(player1, new ElvishLookout()).setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Wall of Glare cannot attack because it has defender")
    void cannotAttack() {
        Permanent wall = addCreatureReady(player1, new WallOfGlare());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wall.isAttacking()).isFalse();
    }
}
