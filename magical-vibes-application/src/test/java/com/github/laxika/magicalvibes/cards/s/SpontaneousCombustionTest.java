package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FoolsTome;
import com.github.laxika.magicalvibes.cards.g.GiantCrab;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("The creature is sacrificed during casting, before damage is dealt")
    void sacrificeIsPaidBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        giveMana();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Wind Drake");
        harness.assertInGraveyard(player1, "Wind Drake");
        harness.assertNotInGraveyard(player1, "Spontaneous Combustion");
        assertThat(survivor.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Horned Turtle");
        assertThat(survivor.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Spontaneous Combustion");
    }

    @Test
    @DisplayName("Resolves normally when the only creature was sacrificed to cast it")
    void resolvesAfterSacrificingOnlyCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        harness.addToBattlefield(player2, new FoolsTome());
        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        giveMana();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wind Drake");
        harness.assertInGraveyard(player1, "Spontaneous Combustion");
        harness.assertOnBattlefield(player2, "Fool's Tome");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new HornedTurtle());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");

        harness.assertOnBattlefield(player1, "Horned Turtle");
        harness.assertOnBattlefield(player2, "Wind Drake");
        harness.assertInHand(player1, "Spontaneous Combustion");
    }

    @Test
    @DisplayName("Shroud does not prevent the untargeted damage")
    void damagesCreatureWithShroud() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        Permanent crab = harness.addToBattlefieldAndReturn(player2, new GiantCrab());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, crab, Keyword.SHROUD)).isTrue();

        harness.setHand(player1, List.of(new SpontaneousCombustion()));
        giveMana();
        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Crab");
        harness.assertInGraveyard(player2, "Giant Crab");
    }
}
