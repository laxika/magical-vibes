package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlewingMystic.class, GrizzlyBears.class})
class BattlewingMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, entering the battlefield does not discard or draw")
    void withoutKickerDoesNotDiscardOrDraw() {
        harness.setHand(player1, List.of(new BattlewingMystic(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With kicker, entering the battlefield discards the hand and draws two cards")
    void withKickerDiscardsHandAndDrawsTwoCards() {
        harness.setHand(player1, List.of(new BattlewingMystic(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("With kicker and an empty hand, entering the battlefield still draws two cards")
    void withKickerAndEmptyHandDrawsTwoCards() {
        harness.setHand(player1, List.of(new BattlewingMystic()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kicked trigger discards the entire current hand only when it resolves")
    void kickedTriggerUsesCurrentHandAndDoesNotAffectOpponent() {
        BattlewingMystic originalHandCard = new BattlewingMystic();
        BattlewingMystic laterHandCard = new BattlewingMystic();
        BattlewingMystic anotherHandCard = new BattlewingMystic();
        BattlewingMystic firstDraw = new BattlewingMystic();
        BattlewingMystic secondDraw = new BattlewingMystic();
        BattlewingMystic remainingLibraryCard = new BattlewingMystic();
        BattlewingMystic opponentHandCard = new BattlewingMystic();
        harness.setHand(player1, List.of(new BattlewingMystic(), originalHandCard));
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, remainingLibraryCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalHandCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(originalHandCard, laterHandCard, anotherHandCard));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(originalHandCard, laterHandCard, anotherHandCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLibraryCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
