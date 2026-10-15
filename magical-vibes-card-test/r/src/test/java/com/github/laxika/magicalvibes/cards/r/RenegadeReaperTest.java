package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeReaper.class, SerraAngel.class, Forest.class})
class RenegadeReaperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills four cards and gains four life when an Angel was milled")
    void gainsLifeWhenAngelIsMilled() {
        harness.setLibrary(player1, List.of(
                new SerraAngel(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("ETB does not gain life when no Angel was milled")
    void doesNotGainLifeWithoutMilledAngel() {
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
        harness.setLibrary(player1, List.of(new SerraAngel(), new Forest()));
        harness.setLife(player1, 20);

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new RenegadeReaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
