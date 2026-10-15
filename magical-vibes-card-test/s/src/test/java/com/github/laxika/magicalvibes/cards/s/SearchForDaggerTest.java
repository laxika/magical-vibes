package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KelethSunmaneFamiliar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearchForDagger.class, GrizzlyBears.class, Forest.class, KelethSunmaneFamiliar.class})
class SearchForDaggerTest extends BaseCardTest {

    @Test
    @DisplayName("A commander entering lets you take a legendary creature from the top six")
    void commanderEnteringSearchesTopSix() {
        Card legendaryCreature = setupSearchAndLibrary();
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);

        harness.enterBattlefieldAndReturn(player1, commander);
        harness.passBothPriorities();

        chooseSearchCard(legendaryCreature);

        assertThat(gd.playerHands.get(player1.getId())).contains(legendaryCreature);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(legendaryCreature);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Attacking with a commander triggers the search")
    void commanderAttacksSearchesTopSix() {
        Card legendaryCreature = setupSearchAndLibrary();
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);

        declareAttackers(List.of(1));
        chooseSearchCard(legendaryCreature);

        assertThat(gd.playerHands.get(player1.getId())).contains(legendaryCreature);
    }

    @Test
    @DisplayName("Noncommanders do not trigger the search")
    void noncommanderDoesNotTrigger() {
        setupSearchAndLibrary();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Card setupSearchAndLibrary() {
        harness.addToBattlefield(player1, new SearchForDagger());
        Card legendaryCreature = new KelethSunmaneFamiliar();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), legendaryCreature,
                new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        return legendaryCreature;
    }

    private void chooseSearchCard(Card legendaryCreature) {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(legendaryCreature);
        harness.handleCardChosen(player1, 0);
    }
}
