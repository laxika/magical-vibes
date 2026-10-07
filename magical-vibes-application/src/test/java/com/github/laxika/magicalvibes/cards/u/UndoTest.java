package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.w.WandOfDenial;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Undo.class, PantherWarriors.class, Python.class, WandOfDenial.class})
class UndoTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoTargetCreatures() {
        UUID pythonId = harness.addToBattlefieldAndReturn(player1, new Python()).getId();
        UUID pantherWarriorsId = harness.addToBattlefieldAndReturn(player2, new PantherWarriors()).getId();

        prepareUndo();
        harness.castAndResolveSorcery(player1, 0, List.of(pythonId, pantherWarriorsId));

        harness.assertNotOnBattlefield(player1, "Python");
        harness.assertNotOnBattlefield(player2, "Panther Warriors");
        harness.assertInHand(player1, "Python");
        harness.assertInHand(player2, "Panther Warriors");
    }

    @Test
    @DisplayName("Returns targeted creatures to their owners' hands even when controlled by another player")
    void returnsTargetCreaturesToOwnersHands() {
        Python ownedByPlayer2 = new Python();
        ownedByPlayer2.setOwnerId(player2.getId());
        Permanent controlledPython = harness.addToBattlefieldAndReturn(player1, ownedByPlayer2);
        Permanent pantherWarriors = harness.addToBattlefieldAndReturn(player2, new PantherWarriors());

        prepareUndo();
        harness.castAndResolveSorcery(player1, 0, List.of(controlledPython.getId(), pantherWarriors.getId()));

        harness.assertNotOnBattlefield(player1, "Python");
        harness.assertNotOnBattlefield(player2, "Panther Warriors");
        harness.assertNotInHand(player1, "Python");
        harness.assertInHand(player2, "Python");
        harness.assertInHand(player2, "Panther Warriors");
    }

    @Test
    @DisplayName("Cannot cast with only one target")
    void cannotCastWithOnlyOneTarget() {
        UUID pythonId = harness.addToBattlefieldAndReturn(player1, new Python()).getId();

        assertThatThrownBy(() -> castUndo(List.of(pythonId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        UUID pythonId = harness.addToBattlefieldAndReturn(player1, new Python()).getId();

        assertThatThrownBy(() -> castUndo(List.of(pythonId, pythonId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        UUID pythonId = harness.addToBattlefieldAndReturn(player1, new Python()).getId();
        UUID wandId = harness.addToBattlefieldAndReturn(player1, new WandOfDenial()).getId();

        assertThatThrownBy(() -> castUndo(List.of(pythonId, wandId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot cast without targets")
    void cannotCastWithoutTargets() {
        harness.addToBattlefield(player1, new Python());
        harness.addToBattlefield(player2, new PantherWarriors());

        assertThatThrownBy(() -> castUndo(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast with three targets")
    void cannotCastWithThreeTargets() {
        UUID firstId = harness.addToBattlefieldAndReturn(player1, new Python()).getId();
        UUID secondId = harness.addToBattlefieldAndReturn(player2, new PantherWarriors()).getId();
        UUID thirdId = harness.addToBattlefieldAndReturn(player2, new Python()).getId();

        assertThatThrownBy(() -> castUndo(List.of(firstId, secondId, thirdId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Returns the remaining legal target when either target leaves before resolution")
    void returnsRemainingLegalTarget(boolean firstTargetLeaves) {
        Permanent python = harness.addToBattlefieldAndReturn(player1, new Python());
        Permanent panther = harness.addToBattlefieldAndReturn(player2, new PantherWarriors());
        harness.addToBattlefield(player2, new WandOfDenial());

        castUndo(List.of(python.getId(), panther.getId()));
        if (firstTargetLeaves) {
            gd.playerBattlefields.get(player1.getId()).remove(python);
            harness.setGraveyard(player1, List.of(python.getCard()));
        } else {
            gd.playerBattlefields.get(player2.getId()).remove(panther);
            harness.setGraveyard(player2, List.of(panther.getCard()));
        }
        harness.passBothPriorities();

        if (firstTargetLeaves) {
            harness.assertInGraveyard(player1, "Python");
            harness.assertNotInHand(player1, "Python");
            harness.assertInHand(player2, "Panther Warriors");
        } else {
            harness.assertInHand(player1, "Python");
            harness.assertInGraveyard(player2, "Panther Warriors");
            harness.assertNotInHand(player2, "Panther Warriors");
        }
        harness.assertNotOnBattlefield(player1, "Python");
        harness.assertNotOnBattlefield(player2, "Panther Warriors");
        harness.assertOnBattlefield(player2, "Wand of Denial");
        harness.assertInGraveyard(player1, "Undo");
    }

    @Test
    @DisplayName("Does not affect other creatures when both targets leave before resolution")
    void doesNotResolveWhenBothTargetsLeave() {
        Permanent python = harness.addToBattlefieldAndReturn(player1, new Python());
        Permanent panther = harness.addToBattlefieldAndReturn(player2, new PantherWarriors());
        harness.addToBattlefield(player2, new Python());

        castUndo(List.of(python.getId(), panther.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(python);
        gd.playerBattlefields.get(player2.getId()).remove(panther);
        harness.setGraveyard(player1, List.of(python.getCard()));
        harness.setGraveyard(player2, List.of(panther.getCard()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Python");
        harness.assertInGraveyard(player2, "Panther Warriors");
        harness.assertInGraveyard(player1, "Undo");
        harness.assertNotInHand(player1, "Python");
        harness.assertNotInHand(player2, "Python");
        harness.assertNotInHand(player2, "Panther Warriors");
        harness.assertOnBattlefield(player2, "Python");
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
