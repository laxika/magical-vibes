package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.v.ViviensGrizzly;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagingKronch.class, ViviensGrizzly.class})
class RagingKronchTest extends BaseCardTest {

    @Test
    @DisplayName("Raging Kronch can't attack alone")
    void cantAttackAlone() {
        addCreatureReady(player1, new RagingKronch());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Raging Kronch can attack alongside another creature")
    void canAttackWithAnother() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RagingKronch());
        addCreatureReady(player1, new ViviensGrizzly());

        declareAttackers(List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Raging Kronch may block alone")
    void canBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new ViviensGrizzly());
        attacker.setAttacking(true);
        Permanent kronch = addCreatureReady(player2, new RagingKronch());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(kronch.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Another creature on the battlefield does not permit Kronch to attack alone")
    void cantAttackAloneWithAnotherCreaturePresent() {
        addCreatureReady(player1, new RagingKronch());
        addCreatureReady(player1, new ViviensGrizzly());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack alone");
    }

    @Test
    @DisplayName("Two Raging Kronches can attack together")
    void twoKronchesCanAttackTogether() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RagingKronch());
        addCreatureReady(player1, new RagingKronch());

        declareAttackers(List.of(0, 1));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Kronch's restriction does not prevent another creature from attacking alone")
    void anotherCreatureCanAttackAlone() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RagingKronch());
        addCreatureReady(player1, new ViviensGrizzly());

        declareAttackers(List.of(1));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's creature does not permit Kronch to attack alone")
    void cantAttackAloneWithOpposingCreaturePresent() {
        addCreatureReady(player1, new RagingKronch());
        addCreatureReady(player2, new ViviensGrizzly());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped companion does not permit Kronch to attack alone")
    void cantAttackAloneWithTappedCompanion() {
        addCreatureReady(player1, new RagingKronch());
        Permanent companion = addCreatureReady(player1, new ViviensGrizzly());
        companion.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
