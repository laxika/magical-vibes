package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadWarlock.class, ChildOfNight.class, RuneclawBear.class, Ornithopter.class})
class DreadWarlockTest extends BaseCardTest {

    @Test
    @DisplayName("Dread Warlock cannot be blocked by a non-black creature")
    void cannotBeBlockedByNonBlackCreature() {
        addCreatureReady(player1, new DreadWarlock());

        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by black creatures");
    }

    @Test
    @DisplayName("Dread Warlock can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        addCreatureReady(player1, new DreadWarlock());

        addCreatureReady(player2, new ChildOfNight());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("A colorless artifact creature cannot block Dread Warlock")
    void cannotBeBlockedByColorlessArtifactCreature() {
        addCreatureReady(player1, new DreadWarlock());
        addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by black creatures");
    }

    @Test
    @DisplayName("Multiple black creatures can block Dread Warlock")
    void canBeBlockedByMultipleBlackCreatures() {
        addCreatureReady(player1, new DreadWarlock());
        addCreatureReady(player2, new ChildOfNight());
        addCreatureReady(player2, new ChildOfNight());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gameLogContains("declares 2 blockers")).isTrue();
    }
}
