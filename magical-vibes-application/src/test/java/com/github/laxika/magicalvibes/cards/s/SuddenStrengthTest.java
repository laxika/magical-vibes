package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
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

@CardUsed({SuddenStrength.class, BorderPatrol.class, KrosanVerge.class})
class SuddenStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+3 and draws a card")
    void boostsAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());
        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.setLibrary(player1, List.of(new BorderPatrol()));
        addMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorderPatrol());
        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.setLibrary(player1, List.of(new BorderPatrol()));
        addMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());
        harness.setHand(player1, List.of(new SuddenStrength()));
        harness.setLibrary(player1, List.of(new BorderPatrol()));
        addMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new SuddenStrength()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
