package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeholdTheMultiverse.class, Forest.class, Island.class, Mountain.class})
class BeholdTheMultiverseTest extends BaseCardTest {

    @Test
    void scriesTwoThenDrawsTwo() {
        Card topCard = new Forest();
        Card secondCard = new Island();
        Card remainingCard = new Mountain();
        BeholdTheMultiverse spell = new BeholdTheMultiverse();
        harness.setLibrary(player1, List.of(topCard, secondCard, remainingCard));
        harness.castFromHand(player1, spell, "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondCard, topCard);
        harness.assertInGraveyard(player1, "Behold the Multiverse");
    }

    @Test
    void foretellsAndCastsOnALaterTurn() {
        BeholdTheMultiverse spell = new BeholdTheMultiverse();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Behold the Multiverse");
    }

    @Test
    void drawsUnseenCardsAfterPuttingBothScryCardsOnBottom() {
        Card first = new Forest();
        Card second = new Island();
        Card third = new Mountain();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.castFromHand(player1, new BeholdTheMultiverse(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        harness.assertInGraveyard(player1, "Behold the Multiverse");
    }

    @Test
    void canDrawCardsPutOnBottomWhenLibraryHasOnlyTwoCards() {
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new BeholdTheMultiverse(), "{3}{U}");
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Behold the Multiverse");
    }

    @Test
    void cannotCastOnTheTurnItWasForetold() {
        BeholdTheMultiverse spell = new BeholdTheMultiverse();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        BeholdTheMultiverse spell = new BeholdTheMultiverse();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Foretell can only be used during your turn");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }
}
