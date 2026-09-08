package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PortentOfCalamity.class, Forest.class, GrizzlyBears.class, Shock.class, PropheticPrism.class})
class PortentOfCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("Offers one optional exile for each card type and continues after a decline")
    void offersOneCardPerType() {
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        Shock shock = new Shock();
        PropheticPrism prism = new PropheticPrism();
        Forest forest = new Forest();
        cast(5, List.of(forest, firstCreature, secondCreature, shock, prism));

        assertThat(librarySearchCards()).containsExactly(forest);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(librarySearchCards()).containsExactlyInAnyOrder(firstCreature, secondCreature);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(librarySearchCards()).containsExactly(shock);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        assertThat(librarySearchCards()).containsExactly(prism);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, shock, prism);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondCreature, forest);
    }

    @Test
    @DisplayName("After exiling four cards, casts one spell for free and puts the rest into hand")
    void castsOneSpellAndReturnsTheRestToHand() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        PropheticPrism prism = new PropheticPrism();
        cast(4, List.of(forest, bears, shock, prism));

        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();
        chooseNextTypeCard();

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), shock.getId(), prism.getId());
        harness.handleMultipleCardsChosen(player1, List.of(prism.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == prism);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, bears, shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void cast(int xValue, List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new PortentOfCalamity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    private List<Card> librarySearchCards() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
    }

    private void chooseNextTypeCard() {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
    }
}
