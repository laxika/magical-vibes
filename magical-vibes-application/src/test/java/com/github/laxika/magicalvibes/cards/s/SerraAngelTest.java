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

@CardUsed({SerraAngel.class, GrizzlyBears.class})
class SerraAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Serra Angel")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(angel.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Serra Angel untapped after attacking")
    void vigilanceKeepsAngelUntappedAfterAttacking() {
        Permanent angel = addCreatureReady(player1, new SerraAngel());

        declareAttackers(List.of(0));

        assertThat(angel.isTapped()).isFalse();
    }
}
