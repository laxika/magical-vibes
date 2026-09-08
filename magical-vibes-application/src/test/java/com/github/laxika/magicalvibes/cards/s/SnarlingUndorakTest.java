package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnarlingUndorak.class, GrizzlyBears.class})
class SnarlingUndorakTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast face down and turned face up for its Morph cost")
    void canBeMorphedFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new SnarlingUndorak()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent undorak = findPermanent(player1, "Snarling Undorak");
        assertThat(undorak.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(undorak));
        harness.passBothPriorities();

        assertThat(undorak.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("The ability gives a target Beast creature +1/+1 until end of turn")
    void boostsTargetBeastCreature() {
        addCreatureReady(player1, new SnarlingUndorak());
        Permanent target = addCreatureReady(player2, new SnarlingUndorak());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SnarlingUndorak());
        Permanent target = addCreatureReady(player1, new SnarlingUndorak());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability cannot target a non-Beast creature")
    void cannotTargetNonBeastCreature() {
        addCreatureReady(player1, new SnarlingUndorak());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
