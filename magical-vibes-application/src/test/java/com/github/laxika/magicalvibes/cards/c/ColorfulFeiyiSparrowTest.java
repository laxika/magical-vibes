package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({ColorfulFeiyiSparrow.class, GrizzlyBears.class})
class ColorfulFeiyiSparrowTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Colorful Feiyi Sparrow")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new ColorfulFeiyiSparrow());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature with flying can block Colorful Feiyi Sparrow")
    void flyingCreatureCanBlockColorfulFeiyiSparrow() {
        addCreatureReady(player1, new ColorfulFeiyiSparrow());
        Permanent blocker = addCreatureReady(player2, new ColorfulFeiyiSparrow());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
