package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpoilsOfTheVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the named card into hand and loses life for each other revealed card")
    void findsNamedCardAndLosesLifeForExiledCards() {
        UUID playerId = player1.getId();
        Card firstMiss = named("First Miss");
        Card secondMiss = named("Second Miss");
        Card hit = named("Hit Card");
        Card leftover = named("Leftover");
        gd.playerDecks.put(playerId, new ArrayList<>(List.of(firstMiss, secondMiss, hit, leftover)));
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Hit Card");

        assertThat(gd.playerHands.get(playerId)).contains(hit);
        assertThat(gd.getPlayerExiledCards(playerId)).containsExactly(firstMiss, secondMiss);
        assertThat(gd.playerDecks.get(playerId)).containsExactly(leftover);
        assertThat(gd.getLife(playerId)).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Exiles the whole library and loses life when the named card is not found")
    void exilesLibraryWhenNamedCardIsMissing() {
        UUID playerId = player1.getId();
        Card first = named("First");
        Card second = named("Second");
        gd.playerDecks.put(playerId, new ArrayList<>(List.of(first, second)));
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Missing Card");

        assertThat(gd.playerHands.get(playerId)).doesNotContain(first, second);
        assertThat(gd.playerDecks.get(playerId)).isEmpty();
        assertThat(gd.getPlayerExiledCards(playerId)).containsExactly(first, second);
        assertThat(gd.getLife(playerId)).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not lose life when the named card is on top")
    void doesNotLoseLifeForImmediateHit() {
        UUID playerId = player1.getId();
        Card hit = named("Hit Card");
        gd.playerDecks.put(playerId, new ArrayList<>(List.of(hit)));
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Hit Card");

        assertThat(gd.playerHands.get(playerId)).contains(hit);
        assertThat(gd.getPlayerExiledCards(playerId)).isEmpty();
        assertThat(gd.getLife(playerId)).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        UUID playerId = player1.getId();
        gd.playerDecks.get(playerId).clear();
        int lifeBefore = gd.getLife(playerId);

        cast();
        harness.handleListChoice(player1, "Missing Card");

        assertThat(gd.getPlayerExiledCards(playerId)).isEmpty();
        assertThat(gd.getLife(playerId)).isEqualTo(lifeBefore);
    }

    private void cast() {
        harness.setHand(player1, List.of(new SpoilsOfTheVault()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private static Card named(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{B}");
        card.setColor(CardColor.BLACK);
        return card;
    }
}
