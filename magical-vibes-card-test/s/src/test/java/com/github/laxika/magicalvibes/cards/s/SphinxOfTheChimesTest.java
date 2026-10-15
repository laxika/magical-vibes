package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxOfTheChimes.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class SphinxOfTheChimesTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding two nonland cards with the same name draws four cards")
    void discardSameNamePairDrawsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SphinxOfTheChimes());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate with two differently named nonland cards")
    void cannotActivateWithDifferentNames() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SphinxOfTheChimes());
        harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Two identically named lands cannot pay the nonland discard cost")
    void cannotActivateWithMatchingLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SphinxOfTheChimes());
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("After the first discard the second pick is locked to the same name")
    void secondDiscardMustMatchFirstName() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SphinxOfTheChimes());
        harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        // Index 0 is now Hill Giant, an illegal pick, so the choice is re-prompted.
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("A lone nonland card cannot be the first discard even when another pair is available")
    void firstDiscardMustHaveMatchingPartner() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SphinxOfTheChimes());
        harness.setHand(player1, List.of(new HillGiant(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertNotInGraveyard(player1, "Hill Giant");

        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        harness.assertInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot activate with only one nonland card")
    void cannotActivateWithOnlyOneNonlandCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SphinxOfTheChimes());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain(), new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tapped summoning-sick Sphinx can activate during its opponent's turn")
    void canActivateWhileTappedDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SphinxOfTheChimes());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(true);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
