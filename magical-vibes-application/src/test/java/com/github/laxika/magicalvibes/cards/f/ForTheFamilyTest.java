package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({ForTheFamily.class, FountainOfYouth.class, GrizzlyBears.class})
class ForTheFamilyTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 when the spell's controller controls fewer than four creatures")
    void givesPlusTwoPlusTwoBelowThreshold() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castForTheFamily(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Target creature gets +4/+4 when the spell's controller controls four creatures")
    void givesPlusFourPlusFourAtThreshold() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        castForTheFamily(target);

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The creature count is checked when the spell resolves")
    void checksCreatureCountAtResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForTheFamily()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        addCreatureReady(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castForTheFamily(target);

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
        harness.setHand(player1, List.of(new ForTheFamily()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castForTheFamily(Permanent target) {
        harness.setHand(player1, List.of(new ForTheFamily()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
