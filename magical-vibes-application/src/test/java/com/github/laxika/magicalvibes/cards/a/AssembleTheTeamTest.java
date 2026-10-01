package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BrainFreeze;
import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.w.WipeClean;
import com.github.laxika.magicalvibes.cards.z.ZealousInquisitor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AssembleTheTeam.class, AvenFarseer.class, WipeClean.class, ZealousInquisitor.class,
        BrainFreeze.class})
class AssembleTheTeamTest extends BaseCardTest {

    @Test
    @DisplayName("Searches only the top third of the library, rounded up")
    void searchesTopThirdOfLibrary() {
        Card firstCard = new AvenFarseer();
        Card secondCard = new WipeClean();
        Card thirdCard = new ZealousInquisitor();
        Card fourthCard = new BrainFreeze();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard, fourthCard));

        harness.castFromHand(player1, new AssembleTheTeam(), "{B}{G}");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getId)
                .containsExactly(firstCard.getId(), secondCard.getId());

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(secondCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstCard.getId(), thirdCard.getId(), fourthCard.getId());
    }
}
