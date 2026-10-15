package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.cards.g.GoForBlood;
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

@CardUsed({PricklyMarmoset.class, Censor.class, Compulsion.class, GrizzlyBears.class, GoForBlood.class})
class PricklyMarmosetTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card gives Prickly Marmoset +2/+0 until end of turn")
    void cyclingAnotherCardBoostsSelf() {
        Permanent marmoset = harness.addToBattlefieldAndReturn(player1, new PricklyMarmoset());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
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
        harness.handleCardChosen(player1, 0);
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
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(marmoset.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(marmoset.getPowerModifier()).isZero();
        assertThat(marmoset.getToughnessModifier()).isZero();
        assertThat(marmoset.getEffectivePower()).isEqualTo(2);
        assertThat(marmoset.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prickly Marmoset cannot be cycled from hand")
    void cannotCycleMarmoset() {
        harness.setHand(player1, List.of(new PricklyMarmoset()));
        harness.setLibrary(player1, List.of(new PricklyMarmoset()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card has no hand-activated ability");

        harness.assertInHand(player1, "Prickly Marmoset");
        harness.assertNotInGraveyard(player1, "Prickly Marmoset");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent cycling a card does not boost Prickly Marmoset")
    void opponentCyclingDoesNotBoostSelf() {
        Permanent marmoset = harness.addToBattlefieldAndReturn(player1, new PricklyMarmoset());
        harness.setHand(player2, List.of(new GoForBlood()));
        harness.setLibrary(player2, List.of(new PricklyMarmoset()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(marmoset.getEffectivePower()).isEqualTo(2);
        assertThat(marmoset.getEffectiveToughness()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Go for Blood");
        harness.assertInHand(player2, "Prickly Marmoset");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each cycling event boosts every controlled Marmoset before drawing")
    void repeatedCyclingBoostsEachMarmoset() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PricklyMarmoset());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PricklyMarmoset());
        harness.setHand(player1, List.of(new GoForBlood(), new GoForBlood()));
        harness.setLibrary(player1, List.of(new PricklyMarmoset(), new PricklyMarmoset()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int cycle = 1; cycle <= 2; cycle++) {
            harness.activateHandAbility(player1, 0, null);
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(first.getEffectivePower()).isEqualTo(2 + 2 * cycle);
            assertThat(second.getEffectivePower()).isEqualTo(2 + 2 * cycle);
            assertThat(first.getEffectiveToughness()).isEqualTo(3);
            assertThat(second.getEffectiveToughness()).isEqualTo(3);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

            harness.passBothPriorities();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
            assertThat(gd.stack).isEmpty();
        }
    }
}
