package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ConsumedByGreed;
import com.github.laxika.magicalvibes.cards.c.CruelclawsHeist;
import com.github.laxika.magicalvibes.cards.c.CrumbAndGetIt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PerchProtection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JollyGerbils.class, CrumbAndGetIt.class, ConsumedByGreed.class, CruelclawsHeist.class, PerchProtection.class,
        Forest.class, GrizzlyBears.class, LlanowarElves.class})
class JollyGerbilsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a promised gift is given")
    void drawsWhenPromisedGiftIsActuallyGiven() {
        Permanent gerbils = addCreatureReady(player1, new JollyGerbils());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.setLibrary(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithGift(player1, 0, gerbils.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Doesn't draw when the gift isn't promised")
    void doesNotDrawWhenGiftIsNotPromised() {
        Permanent gerbils = addCreatureReady(player1, new JollyGerbils());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.setLibrary(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithGift(player1, 0, gerbils.getId(), false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    @DisplayName("Draws only once the gift spell and then its own ability resolve")
    void drawWaitsForGiftAndThenForTriggeredAbilityToResolve() {
        Permanent gerbils = addCreatureReady(player1, new JollyGerbils());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.setLibrary(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithGift(player1, 0, gerbils.getId(), true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Food");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Food");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Crumb and Get It");
    }

    @Test
    @DisplayName("Doesn't draw when an opponent gives a gift")
    void doesNotDrawWhenOpponentGivesGift() {
        addCreatureReady(player1, new JollyGerbils());
        Permanent opponentGerbils = addCreatureReady(player2, new JollyGerbils());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new CrumbAndGetIt()));
        harness.setLibrary(player1, List.of(new CrumbAndGetIt()));
        harness.setLibrary(player2, List.of(new CrumbAndGetIt()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstantWithGift(player2, 0, opponentGerbils.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Each Jolly Gerbils draws for the same gift")
    void eachGerbilsDrawsForTheSameGift() {
        Permanent gerbils = addCreatureReady(player1, new JollyGerbils());
        addCreatureReady(player1, new JollyGerbils());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.setLibrary(player1, List.of(new CrumbAndGetIt(), new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithGift(player1, 0, gerbils.getId(), true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Food");
    }

    @Nested
    @DisplayName("Gifts given by spells that give them without a gift effect of their own")
    class GiftsWithoutGiftEffect {

        @Test
        @DisplayName("Draws when Consumed by Greed's gift is given")
        void drawsForConsumedByGreed() {
            addCreatureReady(player1, new JollyGerbils());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Card returned = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(returned));
            harness.setHand(player1, List.of(new ConsumedByGreed()));
            harness.setLibrary(player1, List.of(new Forest()));
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstantWithGift(player1, 0, null, List.of(player2.getId(), returned.getId()), true);
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertInHand(player1, "Forest");
        }

        @Test
        @DisplayName("Draws when Cruelclaw's Heist's gift is given")
        void drawsForCruelclawsHeist() {
            addCreatureReady(player1, new JollyGerbils());
            Card stolen = new LlanowarElves();
            harness.setHand(player2, List.of(stolen));
            harness.setLibrary(player2, List.of(new Forest()));
            harness.setHand(player1, List.of(new CruelclawsHeist()));
            harness.setLibrary(player1, List.of(new Forest()));
            harness.addMana(player1, ManaColor.BLACK, 2);

            harness.castSorceryWithGift(player1, 0, player2.getId(), true);
            harness.passBothPriorities();
            harness.handleMultipleCardsChosen(player1, List.of(stolen.getId()));
            harness.passBothPriorities();

            harness.assertInHand(player1, "Forest");
        }

        @Test
        @DisplayName("Draws when Perch Protection's gift is given, though the gift phases Jolly Gerbils out")
        void drawsForPerchProtection() {
            addCreatureReady(player1, new JollyGerbils());
            harness.setHand(player1, List.of(new PerchProtection()));
            harness.setLibrary(player1, List.of(new Forest()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 4);

            harness.castInstantWithGift(player1, 0, null, List.of(), true);
            harness.passBothPriorities();
            harness.passBothPriorities();

            harness.assertInHand(player1, "Forest");
        }
    }
}
