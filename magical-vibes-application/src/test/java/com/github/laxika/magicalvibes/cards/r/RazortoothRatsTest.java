package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DancingScimitar;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazortoothRats.class, GrizzlyBears.class, DrudgeSkeletons.class, DancingScimitar.class})
class RazortoothRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Razortooth Rats cannot be blocked by a nonblack nonartifact creature")
    void cannotBeBlockedByNonblackNonartifactCreature() {
        Permanent rats = addCreatureReady(player1, new RazortoothRats());
        rats.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Razortooth Rats can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        Permanent rats = addCreatureReady(player1, new RazortoothRats());
        rats.setAttacking(true);
        addCreatureReady(player2, new DrudgeSkeletons());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Razortooth Rats can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        Permanent rats = addCreatureReady(player1, new RazortoothRats());
        rats.setAttacking(true);
        addCreatureReady(player2, new DancingScimitar());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
