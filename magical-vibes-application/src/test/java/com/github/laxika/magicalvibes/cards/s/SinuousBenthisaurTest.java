package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinuousBenthisaur.class, Plains.class, Forest.class})
class SinuousBenthisaurTest extends BaseCardTest {

    @Test
    @DisplayName("Counts controlled Caves and Cave cards in the controller's graveyard")
    void countsControlledCavesAndCaveCardsInGraveyard() {
        Card battlefieldCave = cave();
        Card opponentCave = cave();
        Card graveyardCave = cave();
        Card topOne = new Forest();
        Card topTwo = new Plains();
        Card topThree = new Forest();

        harness.addToBattlefield(player1, battlefieldCave);
        harness.addToBattlefield(player2, opponentCave);
        harness.setGraveyard(player1, List.of(graveyardCave));
        harness.setLibrary(player1, List.of(topOne, topTwo, topThree));
        castSinuousBenthisaur();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topOne, topTwo);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topThree);
    }

    @Test
    @DisplayName("Puts the unchosen cards on the bottom in a random order")
    void putsUnchosenCardsOnBottomRandomly() {
        Card firstCave = cave();
        Card secondCave = cave();
        Card graveyardCave = cave();
        Card topOne = new Forest();
        Card topTwo = new Plains();
        Card topThree = new Forest();
        Card topFour = new Plains();
        Card bottomCard = new Forest();

        harness.addToBattlefield(player1, firstCave);
        harness.addToBattlefield(player1, secondCave);
        harness.setGraveyard(player1, List.of(graveyardCave));
        harness.setLibrary(player1, List.of(topOne, topTwo, topThree, topFour, bottomCard));
        castSinuousBenthisaur();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(topOne.getId(), topTwo.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topOne, topTwo);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(topThree, topFour, bottomCard);
    }

    private Card cave() {
        Card cave = new Plains().createRuntimeCopy();
        cave.setSubtypes(List.of(CardSubtype.CAVE));
        return cave;
    }

    private void castSinuousBenthisaur() {
        harness.setHand(player1, List.of(new SinuousBenthisaur()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
