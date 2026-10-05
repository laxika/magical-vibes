package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Leashling.class, Forest.class})
class LeashlingTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a card from hand on top of the library returns Leashling to its owner's hand")
    void putsCardOnTopAndReturnsToHand() {
        addCreatureReady(player1, new Leashling());
        Card chosenCard = new Forest();
        Card existingTopCard = new Forest();
        harness.setHand(player1, List.of(chosenCard));
        harness.setLibrary(player1, List.of(existingTopCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leashling");
        harness.assertInHand(player1, "Leashling");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosenCard, existingTopCard);
    }

    @Test
    @DisplayName("Returns itself to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        Permanent leashling = addCreatureReady(player2, new Leashling());
        gd.stolenCreatures.put(leashling.getId(), player1.getId());
        Card chosenCard = new Forest();
        Card existingTopCard = new Forest();
        harness.setHand(player2, List.of(chosenCard));
        harness.setLibrary(player2, List.of(existingTopCard));

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leashling");
        harness.assertInHand(player1, "Leashling");
        harness.assertNotInHand(player2, "Leashling");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosenCard, existingTopCard);
    }

    @Test
    @DisplayName("The ability cannot be activated without a card in hand")
    void cannotActivateWithoutCardInHand() {
        addCreatureReady(player1, new Leashling());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Leashling");
    }

    @Test
    @DisplayName("The chosen card is put on top as a cost before Leashling returns")
    void paysChosenCardBeforeResolution() {
        addCreatureReady(player1, new Leashling());
        Card unchosenCard = new Forest();
        Card chosenCard = new Leashling();
        Card existingTopCard = new Forest();
        harness.setHand(player1, List.of(unchosenCard, chosenCard));
        harness.setLibrary(player1, List.of(existingTopCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(unchosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosenCard, existingTopCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Leashling");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leashling");
        harness.assertInHand(player1, "Leashling");
        assertThat(gd.playerHands.get(player1.getId())).contains(unchosenCard).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosenCard, existingTopCard);
    }

    @Test
    @DisplayName("A tapped summoning-sick Leashling can activate with an empty library")
    void canActivateWhileTappedAndSummoningSickWithEmptyLibrary() {
        Permanent leashling = harness.addToBattlefieldAndReturn(player1, new Leashling());
        leashling.setSummoningSick(true);
        leashling.setTapped(true);
        Card chosenCard = new Forest();
        harness.setHand(player1, List.of(chosenCard));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leashling");
        harness.assertInHand(player1, "Leashling");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosenCard);
    }
}
