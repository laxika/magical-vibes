package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UndoTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoTargetCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        castUndo(List.of(bearsId, spiderId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot cast with only one target")
    void cannotCastWithOnlyOneTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> castUndo(List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.i.Island());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID islandId = harness.getPermanentId(player1, "Island");

        assertThatThrownBy(() -> castUndo(List.of(bearsId, islandId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castUndo(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new Undo()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetIds);
    }
}
