package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
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

@CardUsed({RisenRiptide.class, AcademyDrake.class, GrizzlyBears.class})
class RisenRiptideTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a kicked spell makes Risen Riptide 5/5 until end of turn")
    void kickedSpellMakesItFiveFiveUntilEndOfTurn() {
        Permanent riptide = addReadyRiptide();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(riptide.getEffectivePower()).isEqualTo(5);
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(riptide.getEffectivePower()).isZero();
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Casting a non-kicked spell does not change Risen Riptide")
    void nonKickedSpellDoesNotTrigger() {
        Permanent riptide = addReadyRiptide();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(riptide.getEffectivePower()).isZero();
        assertThat(riptide.getEffectiveToughness()).isEqualTo(5);
    }

    private Permanent addReadyRiptide() {
        Permanent riptide = harness.addToBattlefieldAndReturn(player1, new RisenRiptide());
        riptide.setSummoningSick(false);
        return riptide;
    }
}
