package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HollowMarauder.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class HollowMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when the targeted opponent discards a card with mana value less than four")
    void drawsForLowManaValueDiscard() {
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveCreatureAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when the targeted opponent discards a card with mana value four")
    void doesNotDrawForHighManaValueDiscard() {
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveCreatureAndTrigger();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Draws when the targeted opponent has no card to discard")
    void drawsWhenOpponentHasNoCards() {
        Mountain drawn = new Mountain();
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveCreatureAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Reduces its cost for each creature card in its controller's graveyard")
    void reducesCostForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HollowMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hollow Marauder");
    }

    @Test
    @DisplayName("Only opponents can be targeted")
    void onlyOpponentsCanBeTargeted() {
        harness.setHand(player1, List.of(new HollowMarauder()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
