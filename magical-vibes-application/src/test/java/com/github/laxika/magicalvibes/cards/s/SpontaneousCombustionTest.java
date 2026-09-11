package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FoolsTome;
import com.github.laxika.magicalvibes.cards.g.GiantCrab;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpontaneousCombustion.class, WindDrake.class, GiantCrab.class, HornedTurtle.class, FoolsTome.class})
class SpontaneousCombustionTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Deals 3 damage to each creature after the sacrifice cost is paid")
    void dealsThreeDamageToEachCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        harness.addToBattlefieldAndReturn(player1, new GiantCrab());
        harness.addToBattlefieldAndReturn(player2, new GiantCrab());

        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        giveMana();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wind Drake");
        harness.assertNotOnBattlefield(player1, "Giant Crab");
        harness.assertNotOnBattlefield(player2, "Giant Crab");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Creatures with toughness above 3 survive")
    void toughCreaturesSurvive() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        harness.addToBattlefieldAndReturn(player2, new HornedTurtle());

        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        giveMana();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Horned Turtle");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot use a noncreature to pay the sacrifice cost")
    void cannotSacrificeNoncreature() {
        Permanent tome = harness.addToBattlefieldAndReturn(player1, new FoolsTome());
        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, tome.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
