package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninSkyhunter.class, Frogmite.class})
class LeoninSkyhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Leonin Skyhunter")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new LeoninSkyhunter());
        addCreatureReady(player2, new Frogmite());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }
}
