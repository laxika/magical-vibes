package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({RiveteersOverlook.class, Forest.class, GrizzlyBears.class, Mountain.class, Swamp.class})
class RiveteersOverlookTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices Riveteers Overlook before the search trigger resolves")
    void enteringSacrificesIt() {
        RiveteersOverlook overlook = new RiveteersOverlook();
        harness.setHand(player1, List.of(overlook));

        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(overlook.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(overlook.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(overlook.getId()));
    }

    @Test
    @DisplayName("Searches for a basic Swamp, Mountain, or Forest and puts it onto the battlefield tapped")
    void searchesAllowedBasicLand() {
        playOverlook();
        Card swamp = new Swamp();
        Card mountain = new Mountain();
        Card forest = new Forest();
        setLibrary(swamp, mountain, forest, new GrizzlyBears());

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(swamp, mountain, forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped and controller gains 1 life")
    void chosenLandEntersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        playOverlook();
        Card swamp = new Swamp();
        setLibrary(swamp, new Mountain(), new Forest());

        resolveToSearchPrompt();
        GameData gameData = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        Permanent chosenLand = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(swamp.getId()))
                .findFirst().orElseThrow();
        assertThat(chosenLand.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching land leaves the search resolved and still gains 1 life")
    void noMatchingLandDoesNotPrompt() {
        harness.setLife(player1, 20);
        playOverlook();
        setLibrary(new GrizzlyBears());

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    private void playOverlook() {
        harness.setHand(player1, List.of(new RiveteersOverlook()));
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
