package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShredMemory.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class ShredMemoryTest extends BaseCardTest {

    @Test
    void exilesUpToFourCardsFromOneGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new Shock();
        Card fourth = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new ShredMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId(), fourth.getId());
    }

    @Test
    void canChooseFewerThanFourCards() {
        Card chosen = new GrizzlyBears();
        Card remaining = new LlanowarElves();
        harness.setGraveyard(player1, List.of(chosen, remaining));
        harness.setHand(player1, List.of(new ShredMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(chosen.getId());
    }

    @Test
    void cannotChooseCardsFromTwoGraveyards() {
        Card ownCard = new GrizzlyBears();
        Card opposingCard = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.setHand(player1, List.of(new ShredMemory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(ownCard.getId(), opposingCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        ShredMemory shredMemory = new ShredMemory();
        GrizzlyBears matchingCard = new GrizzlyBears();
        Shock differentManaValue = new Shock();
        harness.setHand(player1, List.of(shredMemory));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Shred Memory");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }
}
