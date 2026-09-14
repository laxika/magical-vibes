package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArjunTheShiftingFlame.class, Forest.class, GrizzlyBears.class})
class ArjunTheShiftingFlameTest extends BaseCardTest {

    @Test
    @DisplayName("When you cast a spell, you put your hand on the bottom and draw that many")
    void putsHandOnBottomAndDrawsThatMany() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card spell = new GrizzlyBears();
        Card kept = new Forest();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(kept);
    }

    @Test
    @DisplayName("The controller orders multiple cards before putting them on the bottom")
    void ordersMultipleCardsOnBottomBeforeDrawing() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card spell = new GrizzlyBears();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card drawnFirst = new Forest();
        Card drawnSecond = new GrizzlyBears();
        Card belowDraws = new Forest();
        harness.setHand(player1, List.of(spell, first, second));
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond, belowDraws));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowDraws, second, first);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a spell")
    void doesNotTriggerForOpponentSpell() {
        harness.addToBattlefield(player1, new ArjunTheShiftingFlame());
        Card spell = new GrizzlyBears();
        Card kept = new Forest();
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player2, List.of(spell, kept));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
