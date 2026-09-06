package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.Compulsion;
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

@CardUsed({PricklyMarmoset.class, Censor.class, Compulsion.class, GrizzlyBears.class})
class PricklyMarmosetTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card gives Prickly Marmoset +2/+0 until end of turn")
    void cyclingAnotherCardBoostsSelf() {
        Permanent marmoset = harness.addToBattlefieldAndReturn(player1, new PricklyMarmoset());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(marmoset.getEffectivePower()).isEqualTo(4);
        assertThat(marmoset.getEffectiveToughness()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A normal discard does not trigger Prickly Marmoset")
    void normalDiscardDoesNotBoostSelf() {
        Permanent marmoset = harness.addToBattlefieldAndReturn(player1, new PricklyMarmoset());
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(marmoset.getEffectivePower()).isEqualTo(2);
        assertThat(marmoset.getEffectiveToughness()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The cycling boost resets at end of turn cleanup")
    void cyclingBoostResetsAtEndOfTurn() {
        Permanent marmoset = harness.addToBattlefieldAndReturn(player1, new PricklyMarmoset());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(marmoset.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(marmoset.getPowerModifier()).isZero();
        assertThat(marmoset.getToughnessModifier()).isZero();
        assertThat(marmoset.getEffectivePower()).isEqualTo(2);
        assertThat(marmoset.getEffectiveToughness()).isEqualTo(3);
    }
}
