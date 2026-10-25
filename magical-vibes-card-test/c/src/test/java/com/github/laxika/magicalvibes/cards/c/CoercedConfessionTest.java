package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BasilicaScreecher;
import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.t.TheWaterCrystal;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoercedConfession.class, BasilicaScreecher.class, ContaminatedGround.class})
class CoercedConfessionTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills four cards and controller draws one per creature milled")
    void millsFourAndDrawsPerCreature() {
        prepare();
        harness.setLibrary(player2, List.of(new BasilicaScreecher(), new ContaminatedGround(), new BasilicaScreecher(), new ContaminatedGround(),
                new ContaminatedGround()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        // The spell itself leaves the hand, so the draws are measured against the post-cast size.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 2);
    }

    @Test
    @DisplayName("No draws when no creature cards are milled")
    void noDrawsWithoutCreatures() {
        prepare();
        harness.setLibrary(player2, List.of(new ContaminatedGround(), new ContaminatedGround(), new ContaminatedGround(), new ContaminatedGround()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1);
    }

    @Test
    @DisplayName("Can target yourself, milling and drawing from your own library")
    void canTargetController() {
        prepare();
        harness.setLibrary(player1, List.of(new BasilicaScreecher(), new BasilicaScreecher(), new ContaminatedGround(), new ContaminatedGround(),
                new ContaminatedGround(), new ContaminatedGround()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // Four milled, two of them creatures, then two of the remaining cards drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(5); // 4 milled + Coerced Confession
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 2);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library is smaller than four")
    void millsOnlyRemainingWhenLibrarySmall() {
        prepare();
        harness.setLibrary(player2, List.of(new BasilicaScreecher(), new ContaminatedGround()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize - 1 + 1);
    }

    @Test
    void emptyLibraryDoesNotDraw() {
        prepare();
        harness.setLibrary(player2, List.of());
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({RestInPeace.class})
    void exiledCreatureCardsDoNotCountForDraws() {
        prepare();
        harness.addToBattlefield(player1, new RestInPeace());
        harness.setLibrary(player2, List.of(new BasilicaScreecher(), new BasilicaScreecher(),
                new ContaminatedGround(), new ContaminatedGround()));
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(4);
    }

    @Test
    @CardUsed({BruvacTheGrandiloquent.class})
    void countsCreaturesInTheEntireDoubledMill() {
        prepare();
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        harness.setLibrary(player2, List.of(new ContaminatedGround(), new ContaminatedGround(),
                new ContaminatedGround(), new ContaminatedGround(), new BasilicaScreecher(),
                new BasilicaScreecher(), new BasilicaScreecher(), new BasilicaScreecher()));
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @CardUsed({TheWaterCrystal.class})
    void countsCreaturesAmongAdditionalMilledCards() {
        prepare();
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.setLibrary(player2, List.of(new ContaminatedGround(), new ContaminatedGround(),
                new ContaminatedGround(), new ContaminatedGround(), new BasilicaScreecher(),
                new BasilicaScreecher(), new BasilicaScreecher(), new BasilicaScreecher()));
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @CardUsed({TheWaterCrystal.class, BruvacTheGrandiloquent.class})
    void drawsForCreaturesMilledInTheOrderTheMillingPlayerChose() {
        prepare();
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            library.add(new ContaminatedGround());
        }
        for (int i = 0; i < 8; i++) {
            library.add(new BasilicaScreecher());
        }
        for (int i = 0; i < 8; i++) {
            library.add(new ContaminatedGround());
        }
        harness.setLibrary(player2, library);
        harness.setLibrary(player1, IntStream.range(0, 10)
                .mapToObj(ignored -> (Card) new ContaminatedGround())
                .toList());

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleListChoice(player2, "Bruvac the Grandiloquent: mill twice that many");

        // Four doubled to eight, plus four: twelve cards, eight of them creatures.
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CoercedConfession()));
        harness.addMana(player1, ManaColor.BLUE, 6);
    }

}
