package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonTurtle.class, GrizzlyBears.class})
class DragonTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps Dragon Turtle and an optional opponent creature and locks their next untap")
    void etbTapsAndLocks() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDragonTurtle(List.of(bears.getId()));

        assertThat(findDragonTurtle()).isNotNull();
        assertThat(findDragonTurtle().isTapped()).isTrue();
        assertThat(findDragonTurtle().getSkipUntapCount()).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB may choose no target")
    void etbMayChooseNoTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DragonTurtle()));
        addDragonTurtleMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findDragonTurtle().isTapped()).isTrue();
        assertThat(findDragonTurtle().getSkipUntapCount()).isEqualTo(1);
        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("ETB cannot target a creature its controller controls")
    void etbCannotTargetOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DragonTurtle()));
        addDragonTurtleMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDragonTurtle(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new DragonTurtle()));
        addDragonTurtleMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addDragonTurtleMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent findDragonTurtle() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DragonTurtle)
                .findFirst()
                .orElseThrow();
    }
}
