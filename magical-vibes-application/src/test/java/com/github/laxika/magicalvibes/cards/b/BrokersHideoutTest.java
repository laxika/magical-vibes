package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrokersHideout.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class BrokersHideoutTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices Brokers Hideout before the search trigger resolves")
    void enteringSacrificesIt() {
        BrokersHideout hideout = new BrokersHideout();
        harness.setHand(player1, List.of(hideout));

        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(hideout.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(hideout.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(hideout.getId()));
    }

    @Test
    @DisplayName("Searches for a basic Forest, Plains, or Island and puts it onto the battlefield tapped")
    void searchesAllowedBasicLand() {
        playHideout();
        Card plains = new Plains();
        Card forest = new Forest();
        Card island = new Island();
        setLibrary(plains, forest, island, new GrizzlyBears());

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains, forest, island);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped and controller gains 1 life")
    void chosenLandEntersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        playHideout();
        Card plains = new Plains();
        setLibrary(plains, new Forest(), new Island());

        resolveToSearchPrompt();
        GameData gameData = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        Permanent chosenLand = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(plains.getId()))
                .findFirst().orElseThrow();
        assertThat(chosenLand.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching land leaves the search resolved and still gains 1 life")
    void noMatchingLandDoesNotPrompt() {
        harness.setLife(player1, 20);
        playHideout();
        setLibrary(new GrizzlyBears());

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    private void playHideout() {
        harness.setHand(player1, List.of(new BrokersHideout()));
        harness.playLand(player1, 0);
    }

    private void resolveToSearchPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
