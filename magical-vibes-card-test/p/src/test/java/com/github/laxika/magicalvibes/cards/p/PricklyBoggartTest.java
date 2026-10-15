package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.e.EarwigSquad;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PricklyBoggart.class, ElvishWarrior.class, EarwigSquad.class, Ornithopter.class})
class PricklyBoggartTest extends BaseCardTest {

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking Prickly Boggart")
    void fearPreventsNonblackNonartifactCreatureFromBlocking() {
        addCreatureReady(player1, new PricklyBoggart());
        addCreatureReady(player2, new ElvishWarrior());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Prickly Boggart (fear)");
    }

    @Test
    @DisplayName("Fear allows a black creature to block Prickly Boggart")
    void fearAllowsBlackCreatureToBlock() {
        addCreatureReady(player1, new PricklyBoggart());
        Permanent blocker = addCreatureReady(player2, new EarwigSquad());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows a nonblack artifact creature to block Prickly Boggart")
    void fearAllowsNonblackArtifactCreatureToBlock() {
        addCreatureReady(player1, new PricklyBoggart());
        Permanent blocker = addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
