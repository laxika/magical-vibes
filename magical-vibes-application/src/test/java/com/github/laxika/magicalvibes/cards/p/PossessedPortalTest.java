package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.StaffOfDomination;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PossessedPortal.class, StaffOfDomination.class})
class PossessedPortalTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a card draw with a skipped draw")
    void skipsDraws() {
        harness.addToBattlefield(player1, new PossessedPortal());
        harness.addToBattlefield(player1, new StaffOfDomination());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StaffOfDomination()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, 4, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each player discards or sacrifices at the beginning of the end step")
    void eachPlayerChoosesDiscardOrSacrifice() {
        harness.addToBattlefield(player1, new PossessedPortal());
        harness.addToBattlefield(player1, new StaffOfDomination());
        harness.addToBattlefield(player2, new StaffOfDomination());
        harness.setHand(player1, List.of(new StaffOfDomination()));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleListChoice(player1,
                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Possessed Portal");
        harness.assertOnBattlefield(player1, "Staff of Domination");
        harness.assertNotOnBattlefield(player2, "Staff of Domination");
        harness.assertInGraveyard(player1, "Staff of Domination");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A player can choose a permanent to sacrifice instead of discarding")
    void choosesPermanentToSacrifice() {
        harness.addToBattlefield(player1, new PossessedPortal());
        var staff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, staff.getId());

        harness.assertOnBattlefield(player1, "Possessed Portal");
        harness.assertNotOnBattlefield(player1, "Staff of Domination");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Replaces an opponent's card draw with a skipped draw")
    void skipsOpponentsDraws() {
        harness.addToBattlefield(player1, new PossessedPortal());
        harness.addToBattlefield(player2, new StaffOfDomination());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new StaffOfDomination()));
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.activateAbility(player2, 0, 4, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Lets a player choose which permanent to sacrifice")
    void choosesWhichPermanentToSacrifice() {
        harness.addToBattlefield(player1, new PossessedPortal());
        var sacrificedStaff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        var keptStaff = harness.addToBattlefieldAndReturn(player1, new StaffOfDomination());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, sacrificedStaff.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(keptStaff)
                .doesNotContain(sacrificedStaff);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
