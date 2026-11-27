package com.github.laxika.magicalvibes.cards.u;

import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.cards.b.BroodhunterWurm;
import com.github.laxika.magicalvibes.cards.c.ClutchOfCurrents;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlamogsNullifier.class, BroodhunterWurm.class, ClutchOfCurrents.class})
class UlamogsNullifierTest extends BaseCardTest {

    @Test
    void movesTwoOpponentOwnedExiledCardsAndCountersTargetSpell() {
        BroodhunterWurm wurm = new BroodhunterWurm();
        ClutchOfCurrents first = new ClutchOfCurrents();
        ClutchOfCurrents second = new ClutchOfCurrents();
        harness.setExile(player2, List.of(first, second));
        castNullifierInResponseTo(wurm);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Broodhunter Wurm");
        harness.assertInGraveyard(player2, "Clutch of Currents");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void decliningLeavesSpellAndExiledCardsUnchanged() {
        BroodhunterWurm wurm = new BroodhunterWurm();
        ClutchOfCurrents first = new ClutchOfCurrents();
        ClutchOfCurrents second = new ClutchOfCurrents();
        harness.setExile(player2, List.of(first, second));
        castNullifierInResponseTo(wurm);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Broodhunter Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    void doesNotPromptWithoutTwoOpponentOwnedExiledCards() {
        BroodhunterWurm wurm = new BroodhunterWurm();
        ClutchOfCurrents onlyCard = new ClutchOfCurrents();
        harness.setExile(player2, List.of(onlyCard));
        castNullifierInResponseTo(wurm);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Broodhunter Wurm");
    }

    @Test
    void ownExiledCardsCannotSupplyTheSecondCard() {
        ClutchOfCurrents ownCard = new ClutchOfCurrents();
        ClutchOfCurrents opponentCard = new ClutchOfCurrents();
        harness.setExile(player1, List.of(ownCard));
        harness.setExile(player2, List.of(opponentCard));

        castNullifierInResponseTo(new BroodhunterWurm());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Broodhunter Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void faceDownOpponentOwnedCardsAreEligibleForProcessing() {
        ClutchOfCurrents first = new ClutchOfCurrents();
        ClutchOfCurrents second = new ClutchOfCurrents();
        gd.addToExile(player2.getId(), first, null, true);
        gd.addToExile(player2.getId(), second, null, true);

        castNullifierInResponseTo(new BroodhunterWurm());

        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    void enteringWithoutASpellToTargetDoesNotProcessCards() {
        ClutchOfCurrents first = new ClutchOfCurrents();
        ClutchOfCurrents second = new ClutchOfCurrents();
        harness.setExile(player2, List.of(first, second));

        harness.castFromHand(player1, new UlamogsNullifier(), "{2}{U}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ulamog's Nullifier");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("The prompt doesn't show cards exiled face down to a player who may not look at them (CR 406.3)")
    void promptHidesFaceDownCards() {
        BroodhunterWurm wurm = new BroodhunterWurm();
        harness.inMutationScope(() -> {
            gd.addToExile(player2.getId(), new ClutchOfCurrents(), null, true);
            gd.addToExile(player2.getId(), new ClutchOfCurrents(), null, true);
        });
        harness.clearMessages();

        castNullifierInResponseTo(wurm);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice.class);
        assertThat(harness.getConn1().getMessagesContaining("Clutch of Currents")).isEmpty();
    }

    @Test
    @DisplayName("A pile of face-down cards is offered as two picks, not card by card (2015-08-25 ruling)")
    void faceDownPileIsOfferedAsTwoPicks() {
        List<ClutchOfCurrents> pile = List.of(new ClutchOfCurrents(), new ClutchOfCurrents(), new ClutchOfCurrents());
        harness.inMutationScope(() -> pile.forEach(card -> gd.addToExile(player2.getId(), card, null, true)));

        castNullifierInResponseTo(new BroodhunterWurm());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice.class,
                        choice -> assertThat(choice.validCardIds()).hasSize(2));
    }

    @Test
    @DisplayName("Two picks of one face-down pile put two different cards of that pile into the graveyard")
    void twoPicksOfOnePileMoveTwoOfItsCards() {
        List<ClutchOfCurrents> pile = List.of(new ClutchOfCurrents(), new ClutchOfCurrents(), new ClutchOfCurrents());
        harness.inMutationScope(() -> pile.forEach(card -> gd.addToExile(player2.getId(), card, null, true)));
        BroodhunterWurm wurm = new BroodhunterWurm();
        castNullifierInResponseTo(wurm);

        var choice = (PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice) gd.interaction.activeInteraction();
        harness.handleMultipleCardsChosen(player1, choice.validCardIds());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Broodhunter Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1).isSubsetOf(pile);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> pile.contains(card)).hasSize(2);
    }

    private void castNullifierInResponseTo(BroodhunterWurm wurm) {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, wurm, "{3}{G}");
        harness.passPriority(player2);
        harness.castFromHand(player1, new UlamogsNullifier(), "{2}{U}{B}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();
    }
}
