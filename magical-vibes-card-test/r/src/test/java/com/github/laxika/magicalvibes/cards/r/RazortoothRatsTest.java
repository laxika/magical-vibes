package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, PhyrexianHulk.class, RazortoothRats.class, ScatheZombies.class})
class RazortoothRatsTest extends BaseCardTest {

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void fearPreventsNonblackNonartifactCreatureFromBlocking() {
        addCreatureReady(player1, new RazortoothRats());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void fearAllowsBlackCreatureToBlock() {
        addCreatureReady(player1, new RazortoothRats());
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void fearAllowsArtifactCreatureToBlock() {
        addCreatureReady(player1, new RazortoothRats());
        Permanent blocker = addCreatureReady(player2, new PhyrexianHulk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows black and artifact creatures to block together")
    void fearAllowsBlackAndArtifactCreaturesToBlockTogether() {
        addCreatureReady(player1, new RazortoothRats());
        Permanent blackBlocker = addCreatureReady(player2, new ScatheZombies());
        Permanent artifactBlocker = addCreatureReady(player2, new PhyrexianHulk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(blackBlocker.isBlocking()).isTrue();
        assertThat(artifactBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A legal blocker does not allow a nonblack nonartifact creature to block too")
    void fearRejectsIllegalBlockerAlongsideLegalBlocker() {
        addCreatureReady(player1, new RazortoothRats());
        addCreatureReady(player2, new ScatheZombies());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear does not restrict which creatures Razortooth Rats can block")
    void fearDoesNotRestrictRatsBlocking() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new RazortoothRats());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
