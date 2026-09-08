package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishConscripts.class, BalduvianBears.class, WallOfGlare.class})
class OrcishConscriptsTest extends BaseCardTest {

    // --- Attacking ---

    @Test
    @DisplayName("Orcish Conscripts can't attack alone")
    void cannotAttackAlone() {
        addCreatureReady(player1, new OrcishConscripts());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures attack");
    }

    @Test
    @DisplayName("Orcish Conscripts can't attack with only one other attacker")
    void cannotAttackWithSingleAlly() {
        addCreatureReady(player1, new OrcishConscripts());
        addCreatureReady(player1, new BalduvianBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures attack");
    }

    @Test
    @DisplayName("Orcish Conscripts can attack when two other creatures also attack")
    void canAttackWithTwoAllies() {
        addCreatureReady(player1, new OrcishConscripts());
        addCreatureReady(player1, new BalduvianBears());
        addCreatureReady(player1, new BalduvianBears());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1, 2))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Orcish Conscripts does not restrict another creature from attacking alone")
    void anotherCreatureCanAttackAlone() {
        addCreatureReady(player1, new OrcishConscripts());
        addCreatureReady(player1, new BalduvianBears());

        assertThatCode(() -> declareAttackers(player1, List.of(1))).doesNotThrowAnyException();
    }

    // --- Blocking ---

    @Test
    @DisplayName("Orcish Conscripts can't block alone")
    void cannotBlockAlone() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new OrcishConscripts());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures block");
    }

    @Test
    @DisplayName("Orcish Conscripts can't block with only one other blocker")
    void cannotBlockWithSingleAlly() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new OrcishConscripts());
        addCreatureReady(player2, new BalduvianBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures block");
    }

    @Test
    @DisplayName("Orcish Conscripts counts other blocking creatures, not block assignments")
    void cannotBlockWithSingleAllyThatBlocksMultipleAttackers() {
        addReadyAttacker(player1);
        addReadyAttacker(player1);
        addCreatureReady(player2, new OrcishConscripts());
        addCreatureReady(player2, new WallOfGlare());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0),
                        new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2 other creatures block");
    }

    @Test
    @DisplayName("Orcish Conscripts can block when two other creatures also block")
    void canBlockWithTwoAllies() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new OrcishConscripts());
        addCreatureReady(player2, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0), new BlockerAssignment(2, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Orcish Conscripts does not restrict another creature from blocking alone")
    void anotherCreatureCanBlockAlone() {
        addReadyAttacker(player1);
        addCreatureReady(player2, new OrcishConscripts());
        addCreatureReady(player2, new BalduvianBears());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    private Permanent addReadyAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new BalduvianBears());
        attacker.setAttacking(true);
        return attacker;
    }
}
