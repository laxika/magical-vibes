package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TribalGolem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GluttonousZombie.class, GlorySeeker.class, GangrenousGoliath.class, TribalGolem.class})
class GluttonousZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Gluttonous Zombie cannot be blocked by non-black non-artifact creatures")
    void cannotBeBlockedByNonBlackNonArtifactCreatures() {
        Permanent attacker = addCreatureReady(player1, new GluttonousZombie());
        attacker.setAttacking(true);

        addCreatureReady(player2, new GlorySeeker());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Gluttonous Zombie can be blocked by black creatures")
    void canBeBlockedByBlackCreatures() {
        Permanent attacker = addCreatureReady(player1, new GluttonousZombie());
        attacker.setAttacking(true);

        addCreatureReady(player2, new GangrenousGoliath());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Gluttonous Zombie can be blocked by artifact creatures")
    void canBeBlockedByArtifactCreatures() {
        Permanent attacker = addCreatureReady(player1, new GluttonousZombie());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new TribalGolem());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }
}
