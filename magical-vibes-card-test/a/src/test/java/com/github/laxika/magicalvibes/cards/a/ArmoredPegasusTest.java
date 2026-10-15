package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KeenEyedArchers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmoredPegasus.class, GrizzlyBears.class, KeenEyedArchers.class})
class ArmoredPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Armored Pegasus")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new ArmoredPegasus());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature with flying can block Armored Pegasus")
    void flyingCreatureCanBlockArmoredPegasus() {
        addCreatureReady(player1, new ArmoredPegasus());
        addCreatureReady(player2, new ArmoredPegasus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Armored Pegasus")
    void reachCreatureCanBlockArmoredPegasus() {
        addCreatureReady(player1, new ArmoredPegasus());
        addCreatureReady(player2, new KeenEyedArchers());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Armored Pegasus can block a creature without flying")
    void canBlockNonFlyingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new ArmoredPegasus());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }
}
