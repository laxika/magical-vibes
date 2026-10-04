package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.m.MiasmicMummy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorrorOfTheBrokenLands.class, Censor.class, MiasmicMummy.class})
class HorrorOfTheBrokenLandsTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card gives this creature +2/+1")
    void cyclingBoostsSelf() {
        harness.addToBattlefield(player1, new HorrorOfTheBrokenLands());
        // Cycling Censor triggers the boost once as a discard.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new HorrorOfTheBrokenLands()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent horror = getHorror();
        assertThat(horror.getPowerModifier()).isEqualTo(2);
        assertThat(horror.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each discard stacks another +2/+1")
    void discardsStack() {
        harness.addToBattlefield(player1, new HorrorOfTheBrokenLands());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new HorrorOfTheBrokenLands(), new HorrorOfTheBrokenLands()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent horror = getHorror();
        assertThat(horror.getPowerModifier()).isEqualTo(4);
        assertThat(horror.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HorrorOfTheBrokenLands());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new HorrorOfTheBrokenLands()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent horror = getHorror();
        assertThat(horror.getPowerModifier()).isEqualTo(2);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(horror.getPowerModifier()).isEqualTo(0);
        assertThat(horror.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Horror can be cycled for one black mana to draw a card")
    void cyclesFromHand() {
        harness.setHand(player1, List.of(new HorrorOfTheBrokenLands()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Horror of the Broken Lands");
        harness.assertNotInHand(player1, "Horror of the Broken Lands");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Censor");
        harness.assertNotOnBattlefield(player1, "Horror of the Broken Lands");
    }

    @Test
    @DisplayName("An opponent cycling does not boost Horror")
    void opponentCyclingDoesNotBoost() {
        harness.addToBattlefield(player1, new HorrorOfTheBrokenLands());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(getHorror().getPowerModifier()).isZero();
        assertThat(getHorror().getToughnessModifier()).isZero();
        harness.assertInHand(player2, "Censor");
    }

    @Test
    @DisplayName("An ordinary discard boosts Horror without cycling or drawing")
    void ordinaryDiscardBoostsSelf() {
        harness.addToBattlefield(player1, new HorrorOfTheBrokenLands());
        harness.setHand(player1, List.of(new MiasmicMummy(), new Censor()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Censor");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(getHorror().getPowerModifier()).isEqualTo(2);
        assertThat(getHorror().getToughnessModifier()).isEqualTo(1);
    }

    private Permanent getHorror() {
        return findPermanent(player1, "Horror of the Broken Lands");
    }
}
