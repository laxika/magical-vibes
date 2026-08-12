package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScaldingTarnTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Scalding Tarn pays 1 life and sacrifices it")
    void activationPaysLifeAndSacrificesIt() {
        harness.addToBattlefield(player1, new ScaldingTarn());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertNotOnBattlefield(player1, "Scalding Tarn");
        harness.assertInGraveyard(player1, "Scalding Tarn");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Search offers only Island or Mountain cards for the untapped battlefield")
    void searchOffersIslandOrMountain() {
        activateSearch();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Island") || card.getName().equals("Mountain"))
                .containsExactlyInAnyOrderElementsOf(List.of(
                        gd.playerDecks.get(player1.getId()).get(0),
                        gd.playerDecks.get(player1.getId()).get(1)));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Island or Mountain enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        String chosenName = search.params().cards().getFirst().getName();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals(chosenName) && !permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find with Scalding Tarn")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Island")
                        || permanent.getCard().getName().equals("Mountain"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new ScaldingTarn());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Island(), new Mountain(), new Forest(), new Plains(), new GrizzlyBears()));
    }
}
