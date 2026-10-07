package com.github.laxika.magicalvibes.cards.u;

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
