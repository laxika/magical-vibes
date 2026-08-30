package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({Shrink.class, SpectralBears.class, SerratedArrows.class})
class ShrinkTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Shrink gives -5/-0 to target creature")
    void resolvesAndWeakensTarget() {
        Permanent bear = addCreatureReady(player1, new SpectralBears());
        harness.setHand(player1, List.of(new Shrink()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(-5);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving Shrink can target an opponent's creature")
    void weakensOpponentsCreature() {
        Permanent bear = addCreatureReady(player2, new SpectralBears());
        harness.setHand(player1, List.of(new Shrink()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(-5);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost from Shrink wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent bear = addCreatureReady(player1, new SpectralBears());
        harness.setHand(player1, List.of(new Shrink()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Shrink")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new SpectralBears()); // legal creature target so the spell is castable (CR 601.2c)
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SerratedArrows());
        harness.setHand(player1, List.of(new Shrink()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
