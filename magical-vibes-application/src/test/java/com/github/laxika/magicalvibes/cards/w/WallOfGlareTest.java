package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfGlare.class, ElvishLookout.class})
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
}
