package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SegovianAngel.class, GrizzlyBears.class})
class SegovianAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Segovian Angel")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SegovianAngel());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A flying creature can block Segovian Angel")
    void flyingCreatureCanBlockSegovianAngel() {
        addCreatureReady(player1, new SegovianAngel());
        Permanent blocker = addCreatureReady(player2, new SegovianAngel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Segovian Angel untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent angel = addCreatureReady(player1, new SegovianAngel());

        declareAttackers(List.of(0));

        assertThat(angel.isTapped()).isFalse();
    }
}
