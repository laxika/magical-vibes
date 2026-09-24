package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GnatMiser;
import com.github.laxika.magicalvibes.cards.o.OboroBreezecaller;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DenseCanopy.class, GnatMiser.class, OboroBreezecaller.class})
class DenseCanopyTest extends BaseCardTest {

    @Test
    @DisplayName("A flying creature can't block a creature without flying")
    void flierCannotBlockGroundCreature() {
        harness.addToBattlefield(player1, new DenseCanopy());
        addCreatureReady(player1, new GnatMiser()).setAttacking(true);
        addCreatureReady(player2, new OboroBreezecaller());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with flying can block only creatures with flying");
    }

    @Test
    @DisplayName("A flying creature can block a creature with flying")
    void flierCanBlockFlier() {
        harness.addToBattlefield(player1, new DenseCanopy());
        addCreatureReady(player1, new OboroBreezecaller()).setAttacking(true);
        addCreatureReady(player2, new OboroBreezecaller());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("A nonflying creature can block a creature without flying")
    void nonflierCanBlockGroundCreature() {
        harness.addToBattlefield(player1, new DenseCanopy());
        addCreatureReady(player1, new GnatMiser()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GnatMiser());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
