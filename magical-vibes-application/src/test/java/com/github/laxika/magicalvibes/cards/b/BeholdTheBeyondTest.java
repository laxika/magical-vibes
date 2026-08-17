package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BeholdTheBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Discards your hand and searches three cards into your hand")
    void discardsHandAndSearchesThreeCards() {
        Card discardedOne = new Forest();
        Card discardedTwo = new Plains();
        Card searchedOne = new Swamp();
        Card searchedTwo = new GrizzlyBears();
        Card searchedThree = new Forest();
        Card libraryRemainder = new Plains();

        harness.setHand(player1, List.of(new BeholdTheBeyond(), discardedOne, discardedTwo));
        harness.setLibrary(player1, List.of(searchedOne, searchedTwo, searchedThree, libraryRemainder));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedOne, discardedTwo);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(3);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(searchedOne, searchedTwo, searchedThree);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryRemainder);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Behold the Beyond");
    }
}
