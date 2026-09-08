package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SultaiSoothsayerTest extends BaseCardTest {

    @Test
    void etbPutsOneOfTopFourIntoHandAndTheRestIntoGraveyard() {
        Card topCard = new Shock();
        Card secondCard = new Forest();
        Card thirdCard = new Island();
        Card fourthCard = new GrizzlyBears();
        Card belowTopFour = new Shock();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard, fourthCard, belowTopFour));

        harness.setHand(player1, List.of(new SultaiSoothsayer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(topCard, secondCard, thirdCard, fourthCard);

        harness.handleMultipleCardsChosen(player1, List.of(thirdCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(topCard, secondCard, fourthCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowTopFour);
    }
}
