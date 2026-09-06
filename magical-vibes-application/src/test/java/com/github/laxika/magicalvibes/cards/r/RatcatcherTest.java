package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ratcatcher.class, BogRats.class, GrizzlyBears.class})
class RatcatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may search the library for a Rat")
    void searchesForRat() {
        harness.addToBattlefield(player1, new Ratcatcher());
        harness.setHand(player1, List.of());
        BogRats rat = new BogRats();
        GrizzlyBears nonRat = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonRat, rat));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(rat);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(rat);
        assertThat(gd.playerDecks.get(player1.getId())).contains(nonRat).doesNotContain(rat);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not search")
    void declinesSearch() {
        harness.addToBattlefield(player1, new Ratcatcher());
        BogRats rat = new BogRats();
        harness.setLibrary(player1, List.of(rat));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(rat);
        assertThat(gd.playerDecks.get(player1.getId())).contains(rat);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Ratcatcher());
        BogRats rat = new BogRats();
        harness.setLibrary(player1, List.of(rat));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(rat);
    }
}
