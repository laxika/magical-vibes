package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({SugarRush.class, GrizzlyBears.class, Island.class})
class SugarRushTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+0 and draws a card")
    void boostsTargetAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        castOn(target);

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castOn(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new SugarRush()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new SugarRush()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
