package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.w.WandOfDenial;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Undo.class, PantherWarriors.class, Python.class, WandOfDenial.class})
class UndoTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoTargetCreatures() {
        harness.addToBattlefield(player1, new Python());
        harness.addToBattlefield(player2, new PantherWarriors());
        UUID pythonId = harness.getPermanentId(player1, "Python");
        UUID pantherWarriorsId = harness.getPermanentId(player2, "Panther Warriors");

        prepareUndo();
        harness.castAndResolveSorcery(player1, 0, List.of(pythonId, pantherWarriorsId));

        harness.assertNotOnBattlefield(player1, "Python");
        harness.assertNotOnBattlefield(player2, "Panther Warriors");
        harness.assertInHand(player1, "Python");
        harness.assertInHand(player2, "Panther Warriors");
    }

    @Test
    @DisplayName("Cannot cast with only one target")
    void cannotCastWithOnlyOneTarget() {
        harness.addToBattlefield(player1, new Python());
        UUID pythonId = harness.getPermanentId(player1, "Python");

        assertThatThrownBy(() -> castUndo(List.of(pythonId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        harness.addToBattlefield(player1, new Python());
        UUID pythonId = harness.getPermanentId(player1, "Python");

        assertThatThrownBy(() -> castUndo(List.of(pythonId, pythonId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new Python());
        harness.addToBattlefield(player1, new WandOfDenial());
        UUID pythonId = harness.getPermanentId(player1, "Python");
        UUID wandId = harness.getPermanentId(player1, "Wand of Denial");

        assertThatThrownBy(() -> castUndo(List.of(pythonId, wandId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castUndo(List<UUID> targetIds) {
        prepareUndo();
        harness.castSorcery(player1, 0, targetIds);
    }

    private void prepareUndo() {
        harness.setHand(player1, List.of(new Undo()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
