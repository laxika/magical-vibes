package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndyingBeast.class, WrathOfGod.class})
class UndyingBeastTest extends BaseCardTest {

    @Test
    @DisplayName("When Undying Beast dies, it is put on top of its owner's library instead of staying in the graveyard")
    void diesGoesToTopOfLibrary() {
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new UndyingBeast());
        Card beastCard = beast.getCard();

        // Player 1 wraths the board — Undying Beast dies and its ON_DEATH trigger goes on the stack.
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Wrath resolves — Beast dies, death trigger placed
        harness.passBothPriorities(); // resolve the death trigger — tuck on top of library

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(beastCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(beastCard.getId()));
    }

    @Test
    @DisplayName("When a player controls an opponent-owned Undying Beast, it goes to its owner's library")
    void diesGoesToOwnersLibraryWhenControlledByAnotherPlayer() {
        Card beastCard = new UndyingBeast();
        beastCard.setOwnerId(player2.getId());
        Card player1LibraryCard = new WrathOfGod();
        Card player2LibraryCard = new WrathOfGod();
        harness.setLibrary(player1, List.of(player1LibraryCard));
        harness.setLibrary(player2, List.of(player2LibraryCard));
        harness.addToBattlefield(player1, beastCard);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId())
                .isEqualTo(beastCard.getId());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(player1LibraryCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(beastCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(beastCard.getId()));
    }

    @Test
    @DisplayName("The death trigger puts Undying Beast into an empty library only when it resolves")
    void deathTriggerResolvesIntoEmptyLibrary() {
        Card beastCard = new UndyingBeast();
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, beastCard);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Undying Beast");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId).containsExactly(beastCard.getId());
        harness.assertNotInGraveyard(player1, "Undying Beast");
    }

    @Test
    @DisplayName("The death trigger does nothing if Undying Beast has left the graveyard")
    void deathTriggerDoesNotRetrieveCardFromExile() {
        Card beastCard = new UndyingBeast();
        Card libraryCard = new WrathOfGod();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addToBattlefield(player1, beastCard);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Undying Beast");

        // Model the source being exiled while its death trigger is pending.
        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card.getId().equals(beastCard.getId()));
        harness.setExile(player1, List.of(beastCard));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId).containsExactly(libraryCard.getId());
        harness.assertNotInGraveyard(player1, "Undying Beast");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(beastCard.getId());
    }
}
