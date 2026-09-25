package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerraAngel.class, GrizzlyBears.class, CanopySpider.class})
class SerraAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Serra Angel")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(angel.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A creature with flying can block Serra Angel")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new SerraAngel());
        Permanent blocker = addCreatureReady(player2, new SerraAngel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Serra Angel")
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new SerraAngel());
        Permanent blocker = addCreatureReady(player2, new CanopySpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Serra Angel untapped after attacking")
    void vigilanceKeepsAngelUntappedAfterAttacking() {
        Permanent angel = addCreatureReady(player1, new SerraAngel());

        declareAttackers(List.of(0));

        assertThat(angel.isTapped()).isFalse();
    }
}
