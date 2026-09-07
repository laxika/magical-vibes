package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisterNegative.class, Island.class})
class MisterNegativeTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges life totals and draws the life lost")
    void exchangesAndDrawsLifeLost() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 7);
        harness.setLibrary(player1, List.of(
                new Island(), new Island(), new Island(), new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island(), new Island(), new Island(), new Island(),
                new Island()));

        castMisterNegative();
        chooseOpponentAndResolve(true);

        harness.assertLife(player1, 7);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(13);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when the controller gains life")
    void doesNotDrawWhenControllerGainsLife() {
        harness.setLife(player1, 7);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Island()));

        castMisterNegative();
        chooseOpponentAndResolve(true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 7);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining leaves both life totals unchanged")
    void decliningLeavesLifeTotalsUnchanged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 7);

        castMisterNegative();
        chooseOpponentAndResolve(false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 7);
    }

    @Test
    @DisplayName("Only an opponent is offered as the target")
    void onlyOpponentIsTargetable() {
        harness.setHand(player1, List.of(new MisterNegative()));
        addManaForCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
    }

    private void castMisterNegative() {
        harness.setHand(player1, List.of(new MisterNegative()));
        addManaForCast();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseOpponentAndResolve(boolean accept) {
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
