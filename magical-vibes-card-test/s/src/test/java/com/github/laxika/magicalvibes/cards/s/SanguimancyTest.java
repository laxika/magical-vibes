package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArchetypeOfFinality;
import com.github.laxika.magicalvibes.cards.b.BileBlight;
import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sanguimancy.class, ArchetypeOfFinality.class, FelhideBrawler.class, BileBlight.class})
@DisplayName("Sanguimancy")
class SanguimancyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and loses life equal to black devotion")
    void drawsAndLosesLifeEqualToBlackDevotion() {
        harness.addToBattlefield(player1, new ArchetypeOfFinality());
        harness.setLibrary(player1, List.of(new FelhideBrawler(), new FelhideBrawler(), new FelhideBrawler()));
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new Sanguimancy(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Sanguimancy");
    }

    @Test
    @DisplayName("Does nothing when black devotion is zero")
    void doesNothingWhenBlackDevotionIsZero() {
        harness.setLibrary(player1, List.of(new FelhideBrawler(), new FelhideBrawler()));
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new Sanguimancy(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent permanents and cards outside the battlefield do not contribute devotion")
    void ignoresOpponentPermanentsAndCardsOutsideBattlefield() {
        harness.addToBattlefield(player2, new ArchetypeOfFinality());
        harness.setGraveyard(player1, List.of(new ArchetypeOfFinality()));
        harness.setLibrary(player1, List.of(new FelhideBrawler(), new FelhideBrawler()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Sanguimancy(), new ArchetypeOfFinality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts black symbols across all controlled permanents")
    void sumsDevotionAcrossControlledPermanents() {
        harness.addToBattlefield(player1, new ArchetypeOfFinality());
        harness.addToBattlefield(player1, new FelhideBrawler());
        harness.setLibrary(player1, List.of(new FelhideBrawler(), new FelhideBrawler(),
                new FelhideBrawler(), new FelhideBrawler()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new Sanguimancy(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses devotion at resolution after a permanent is removed in response")
    void usesDevotionAtResolution() {
        harness.addToBattlefield(player1, new FelhideBrawler());
        harness.setLibrary(player1, List.of(new FelhideBrawler(), new FelhideBrawler()));
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new BileBlight()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castFromHand(player1, new Sanguimancy(), "{4}{B}");
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Felhide Brawler"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Felhide Brawler");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Sanguimancy");
    }
}
