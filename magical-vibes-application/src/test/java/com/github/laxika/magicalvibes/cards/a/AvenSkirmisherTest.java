package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenSkirmisher.class, ArashinCleric.class, ArchersOfQarsi.class})
class AvenSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("A creature without flying or reach cannot block Aven Skirmisher")
    void nonflyingCreatureCannotBlock() {
        addCreatureReady(player1, new AvenSkirmisher());
        addCreatureReady(player2, new ArashinCleric());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature with flying can block Aven Skirmisher")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new AvenSkirmisher());
        Permanent blocker = addCreatureReady(player2, new AvenSkirmisher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Aven Skirmisher")
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new AvenSkirmisher());
        Permanent blocker = addCreatureReady(player2, new ArchersOfQarsi());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Aven Skirmisher can block a creature without flying")
    void canBlockNonflyingCreature() {
        addCreatureReady(player1, new ArashinCleric());
        Permanent blocker = addCreatureReady(player2, new AvenSkirmisher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
