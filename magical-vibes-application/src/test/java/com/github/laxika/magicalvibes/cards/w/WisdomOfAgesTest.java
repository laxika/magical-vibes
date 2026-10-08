package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WisdomOfAges.class, Brainstorm.class, Divination.class, GrizzlyBears.class})
class WisdomOfAgesTest extends BaseCardTest {

    private void addWisdomOfAgesMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 3);
    }

    @Test
    @DisplayName("Returns all instants and sorceries from the graveyard, but not permanents")
    void returnsAllInstantsAndSorceries() {
        Card instant = new Brainstorm();
        Card sorcery = new Divination();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, sorcery, creature));

        WisdomOfAges wisdom = new WisdomOfAges();
        harness.setHand(player1, List.of(wisdom));
        addWisdomOfAgesMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(instant, sorcery);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Grants no maximum hand size for the rest of the game and exiles itself")
    void grantsNoMaximumHandSizeAndExilesItself() {
        WisdomOfAges wisdom = new WisdomOfAges();
        harness.setHand(player1, List.of(wisdom));
        addWisdomOfAgesMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wisdom);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wisdom);
    }

    @Test
    @DisplayName("Returns every other copy of Wisdom of Ages only from the controller's graveyard")
    void returnsOtherCopiesWithoutTouchingOpponentsGraveyard() {
        WisdomOfAges first = new WisdomOfAges();
        WisdomOfAges second = new WisdomOfAges();
        WisdomOfAges opponentCard = new WisdomOfAges();
        WisdomOfAges spell = new WisdomOfAges();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of());
        addWisdomOfAgesMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.playersWithNoMaximumHandSize).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Keeps a hand above seven cards through cleanup and the controller's following turn")
    void noMaximumHandSizePersistsAcrossTurns() {
        List<Card> returnedCards = IntStream.range(0, 9)
                .mapToObj(i -> (Card) new WisdomOfAges()).toList();
        harness.setGraveyard(player1, returnedCards);
        harness.setHand(player1, List.of(new WisdomOfAges()));
        addWisdomOfAgesMana();
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(returnedCards);

        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);
        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(returnedCards);
    }
}
