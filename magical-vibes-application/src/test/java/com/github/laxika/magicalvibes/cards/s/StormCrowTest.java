package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormCrow.class, SwornDefender.class})
class StormCrowTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Storm Crow")
    void flyingPreventsNonflyingCreatureFromBlocking() {
        addCreatureReady(player1, new StormCrow());
        addCreatureReady(player2, new SwornDefender());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature with flying can block Storm Crow")
    void flyingCreatureCanBlockStormCrow() {
        addCreatureReady(player1, new StormCrow());
        addCreatureReady(player2, new StormCrow());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }
}
