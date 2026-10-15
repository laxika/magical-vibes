package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonTurtle.class, HillGiantHerdgorger.class})
class DragonTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps Dragon Turtle and an optional opponent creature and locks their next untap")
    void etbTapsAndLocks() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        castDragonTurtle(List.of(creature.getId()));

        assertThat(findDragonTurtle()).isNotNull();
        assertThat(findDragonTurtle().isTapped()).isTrue();
        assertThat(findDragonTurtle().getSkipUntapCount()).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB may choose no target")
    void etbMayChooseNoTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        harness.setHand(player1, List.of(new DragonTurtle()));
        addDragonTurtleMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findDragonTurtle().isTapped()).isTrue();
        assertThat(findDragonTurtle().getSkipUntapCount()).isEqualTo(1);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("ETB cannot target a creature its controller controls")
    void etbCannotTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        harness.setHand(player1, List.of(new DragonTurtle()));
        addDragonTurtleMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each creature skips only its own controller's next untap")
    void restrictionsExpireIndependently() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        castDragonTurtle(List.of(creature.getId()));
        Permanent turtle = findDragonTurtle();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getSkipUntapCount()).isZero();
        assertThat(turtle.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player1);
        assertThat(turtle.isTapped()).isTrue();
        assertThat(turtle.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isFalse();
        assertThat(turtle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An illegal sole target causes the entire trigger to do nothing")
    void illegalTargetDoesNotTapTurtle() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new DragonTurtle()));
        addDragonTurtleMana();
        harness.castCreature(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(findDragonTurtle().isTapped()).isFalse();
        assertThat(findDragonTurtle().getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Already tapped creatures still receive the untap restriction")
    void alreadyTappedTargetIsLocked() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        creature.tap();
        castDragonTurtle(List.of(creature.getId()));

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The opponent creature is still tapped and locked if Dragon Turtle leaves")
    void sourceLeavingDoesNotStopTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new DragonTurtle()));
        addDragonTurtleMana();
        harness.castCreature(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        Permanent turtle = findDragonTurtle();
        gd.playerBattlefields.get(player1.getId()).remove(turtle);
        harness.setGraveyard(player1, List.of(turtle.getCard()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
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
