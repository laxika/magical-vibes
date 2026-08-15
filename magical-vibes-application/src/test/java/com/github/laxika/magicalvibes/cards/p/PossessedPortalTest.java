package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OathOfLimDL;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PossessedPortalTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a card draw with a skipped draw")
    void skipsDraws() {
        harness.addToBattlefield(player1, new PossessedPortal());
        harness.addToBattlefield(player1, new OathOfLimDL());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each player discards or sacrifices at the beginning of the end step")
    void eachPlayerChoosesDiscardOrSacrifice() {
        harness.addToBattlefield(player1, new PossessedPortal());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleListChoice(player1,
                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Possessed Portal");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A player can choose a permanent to sacrifice instead of discarding")
    void choosesPermanentToSacrifice() {
        harness.addToBattlefield(player1, new PossessedPortal());
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Possessed Portal");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
