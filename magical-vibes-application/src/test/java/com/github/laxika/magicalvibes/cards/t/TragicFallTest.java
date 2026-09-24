package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

@CardUsed({TragicFall.class, GrizzlyBears.class, FountainOfYouth.class})
class TragicFallTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -3/-3 when the controller has cards in hand")
    void givesMinusThreeMinusThreeWithCardsInHand() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall(), new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives target creature -13/-13 with an empty hand")
    void givesMinusThirteenMinusThirteenWithEmptyHand() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Checks hellbent when the spell resolves")
    void checksHellbentAtResolution() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = addFourFourTarget();
        harness.setHand(player1, List.of(new TragicFall(), new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TragicFall()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addFourFourTarget() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(4);
        targetCard.setToughness(4);
        return harness.addToBattlefieldAndReturn(player2, targetCard);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
