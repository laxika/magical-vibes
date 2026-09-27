package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.Vermiculos;
import com.github.laxika.magicalvibes.cards.w.WailOfTheNim;
import com.github.laxika.magicalvibes.cards.w.WallOfBlood;
import com.github.laxika.magicalvibes.cards.w.WrenchMind;
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

    private void cast() {
        harness.castFromHand(player1, new SpoilsOfTheVault(), "{B}");
        harness.passBothPriorities();
    }
}
