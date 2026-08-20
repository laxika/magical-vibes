package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbermouthSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Dragon, the accepted ETB search puts a basic land on top")
    void searchesBasicLandToTopWithoutDragon() {
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));

        acceptEtbSearch();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().cards()).containsExactly(basicLand);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With a Dragon, the accepted ETB search puts a basic land onto the battlefield tapped")
    void searchesBasicLandToBattlefieldTappedWithDragon() {
        harness.addToBattlefield(player1, new ShivanDragon());
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));

        acceptEtbSearch();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent land = findPermanent(player1, basicLand.getName());
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB search does nothing")
    void declinesEtbSearch() {
        Card basicLand = new Plains();
        castSentinel(List.of(basicLand));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSentinel(List<Card> library) {
        harness.setHand(player1, List.of(new EmbermouthSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(library);
        harness.castCreature(player1, 0);
    }

    private void acceptEtbSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
