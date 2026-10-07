package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.Vermiculos;
import com.github.laxika.magicalvibes.cards.w.WailOfTheNim;
import com.github.laxika.magicalvibes.cards.w.WallOfBlood;
import com.github.laxika.magicalvibes.cards.w.WrenchMind;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpoilsOfTheVault.class, WallOfBlood.class, WailOfTheNim.class,
        WrenchMind.class, Vermiculos.class})
class SpoilsOfTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the named card into hand and loses life for each other revealed card")
    void findsNamedCardAndLosesLifeForExiledCards() {
        UUID playerId = player1.getId();
        WallOfBlood firstMiss = new WallOfBlood();
        WailOfTheNim secondMiss = new WailOfTheNim();
        WrenchMind hit = new WrenchMind();
        Vermiculos leftover = new Vermiculos();
        harness.setLibrary(player1, List.of(firstMiss, secondMiss, hit, leftover));
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Wrench Mind");

        assertThat(gd.playerHands.get(playerId)).contains(hit);
        assertThat(gd.getPlayerExiledCards(playerId)).containsExactly(firstMiss, secondMiss);
        assertThat(gd.playerDecks.get(playerId)).containsExactly(leftover);
        assertThat(gd.getLife(playerId)).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Exiles the whole library and loses life when the named card is not found")
    void exilesLibraryWhenNamedCardIsMissing() {
        UUID playerId = player1.getId();
        WallOfBlood first = new WallOfBlood();
        WailOfTheNim second = new WailOfTheNim();
        harness.setLibrary(player1, List.of(first, second));
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Wrench Mind");

        assertThat(gd.playerHands.get(playerId)).doesNotContain(first, second);
        assertThat(gd.playerDecks.get(playerId)).isEmpty();
        assertThat(gd.getPlayerExiledCards(playerId)).containsExactly(first, second);
        assertThat(gd.getLife(playerId)).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not lose life when the named card is on top")
    void doesNotLoseLifeForImmediateHit() {
        UUID playerId = player1.getId();
        WrenchMind hit = new WrenchMind();
        harness.setLibrary(player1, List.of(hit));
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Wrench Mind");

        assertThat(gd.playerHands.get(playerId)).contains(hit);
        assertThat(gd.getPlayerExiledCards(playerId)).isEmpty();
        assertThat(gd.getLife(playerId)).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        UUID playerId = player1.getId();
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Wrench Mind");

        assertThat(gd.getPlayerExiledCards(playerId)).isEmpty();
        assertThat(gd.getLife(playerId)).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Stops at the first copy of the named card")
    void leavesLaterCopiesInLibrary() {
        UUID playerId = player1.getId();
        WallOfBlood miss = new WallOfBlood();
        WrenchMind firstCopy = new WrenchMind();
        WrenchMind secondCopy = new WrenchMind();
        harness.setLibrary(player1, List.of(miss, firstCopy, secondCopy));
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Wrench Mind");

        assertThat(gd.playerHands.get(playerId)).containsExactly(firstCopy);
        assertThat(gd.playerDecks.get(playerId)).containsExactly(secondCopy);
        assertThat(gd.getPlayerExiledCards(playerId)).containsExactly(miss);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Finishes moving cards before lethal life loss ends the game")
    void lethalLifeLossFinishesResolution() {
        UUID playerId = player1.getId();
        WallOfBlood firstMiss = new WallOfBlood();
        WailOfTheNim secondMiss = new WailOfTheNim();
        WrenchMind hit = new WrenchMind();
        harness.setLibrary(player1, List.of(firstMiss, secondMiss, hit));
        harness.setLife(player1, 1);

        cast();
        harness.handleListChoice(player1, "Wrench Mind");

        assertThat(gd.playerHands.get(playerId)).containsExactly(hit);
        assertThat(gd.getPlayerExiledCards(playerId)).containsExactly(firstMiss, secondMiss);
        assertThat(gd.playerDecks.get(playerId)).isEmpty();
        harness.assertLife(player1, -1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Card-name suggestions do not disclose the opponent's hidden cards")
    void nameSuggestionsAreIndependentOfOpponentsHiddenHand() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new WallOfBlood()));
        harness.clearMessages();

        cast();
        var mapper = new JacksonConfig().objectMapper();
        var firstPrompt = harness.getConn1().getMessagesContaining("Choose a card name.");
        assertThat(firstPrompt).isNotEmpty();
        var firstOptions = mapper.readTree(firstPrompt.getLast()).get("options");
        harness.handleListChoice(player1, "Wrench Mind");

        harness.setHand(player2, List.of(new WailOfTheNim()));
        harness.clearMessages();
        cast();
        var secondPrompt = harness.getConn1().getMessagesContaining("Choose a card name.");
        assertThat(secondPrompt).isNotEmpty();
        var secondOptions = mapper.readTree(secondPrompt.getLast()).get("options");
        harness.handleListChoice(player1, "Wrench Mind");

        assertThat(secondOptions).isEqualTo(firstOptions);
    }

    private void cast() {
        harness.castFromHand(player1, new SpoilsOfTheVault(), "{B}");
        harness.passBothPriorities();
    }
}
