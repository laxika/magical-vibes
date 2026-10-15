package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DancingScimitar;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GluttonousZombie.class, GlorySeeker.class, DrudgeSkeletons.class, DancingScimitar.class})
class GluttonousZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Gluttonous Zombie cannot be blocked by non-black non-artifact creatures")
    void cannotBeBlockedByNonBlackNonArtifactCreatures() {
        addCreatureReady(player1, new GluttonousZombie());

        addCreatureReady(player2, new GlorySeeker());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Gluttonous Zombie can be blocked by black creatures")
    void canBeBlockedByBlackCreatures() {
        addCreatureReady(player1, new GluttonousZombie());

        Permanent blocker = addCreatureReady(player2, new DrudgeSkeletons());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gluttonous Zombie can be blocked by artifact creatures")
    void canBeBlockedByArtifactCreatures() {
        addCreatureReady(player1, new GluttonousZombie());

        Permanent blocker = addCreatureReady(player2, new DancingScimitar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear does not restrict Gluttonous Zombie from blocking")
    void fearDoesNotRestrictBlocking() {
        addCreatureReady(player1, new GlorySeeker());
        Permanent blocker = addCreatureReady(player2, new GluttonousZombie());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
