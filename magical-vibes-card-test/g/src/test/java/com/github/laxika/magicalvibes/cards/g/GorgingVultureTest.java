package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SilverbackShaman;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GorgingVulture.class, Forest.class, SilverbackShaman.class})
class GorgingVultureTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills four cards and gains one life per creature milled")
    void millsFourAndGainsLifePerCreature() {
        harness.setLibrary(player1, List.of(
                new SilverbackShaman(), new Forest(), new SilverbackShaman(), new Forest(), new Forest()));
        harness.setLife(player1, 20);

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("ETB gains no life when no creature cards are milled")
    void gainsNoLifeWithoutMilledCreatures() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB mills only the cards remaining in a short library")
    void millsRemainingCardsWhenLibraryIsShort() {
        harness.setLibrary(player1, List.of(new SilverbackShaman(), new Forest()));
        harness.setLife(player1, 20);

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("ETB with an empty library neither gains life nor loses the game")
    void emptyLibraryDoesNotGainLifeOrLoseGame() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Gorging Vulture");
    }

    @Test
    @DisplayName("ETB counts only creatures milled this way and leaves the opponent unaffected")
    void ignoresCreaturesAlreadyInGraveyardAndOpponentsLibrary() {
        harness.setGraveyard(player1, List.of(new SilverbackShaman(), new SilverbackShaman()));
        harness.setLibrary(player1, List.of(new SilverbackShaman(), new Forest(), new Forest(), new Forest()));
        var opponentsLibrary = List.of(new SilverbackShaman(), new Forest());
        harness.setLibrary(player2, opponentsLibrary);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentsLibrary);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new GorgingVulture()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
