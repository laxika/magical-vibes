package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({RowdyCrew.class, GrizzlyBears.class, Forest.class, Shock.class, Abundance.class})
class RowdyCrewTest extends BaseCardTest {


    @Test
    @DisplayName("ETB draws 3 cards and discards 2 at random (net +1 card in hand)")
    void etbDrawsThreeDiscardsTwo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Set up deck with 3 creatures so we know what type is drawn
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new RowdyCrew()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // Drew 3, discarded 2 → 1 card in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck should be empty (had 3, drew 3)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // 2 random discards should be logged
        long randomDiscardLogs = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(2);
    }


    @Test
    @DisplayName("Gets +1/+1 counters when discarded cards share a card type (all creatures)")
    void getsCountersWhenDiscardedShareType() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // All 3 drawn cards are creatures — any 2 discarded will share type
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new RowdyCrew()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent rowdyCrew = findPermanent(player1, "Rowdy Crew");

        // Should have 2 +1/+1 counters → 5/5
        assertThat(rowdyCrew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(rowdyCrew.getEffectivePower()).isEqualTo(5);
        assertThat(rowdyCrew.getEffectiveToughness()).isEqualTo(5);
    }


    @Test
    @DisplayName("No counters when discarded cards do not share a card type")
    void noCountersWhenDiscardedDontShareType() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Deck has exactly 1 creature and 1 land — draws only 2 (deck runs out),
        // then discards both at random. Creature and land don't share a card type.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.setHand(player1, List.of(new RowdyCrew()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());

        Permanent rowdyCrew = findPermanent(player1, "Rowdy Crew");

        // No counters — creature and land don't share a card type
        assertThat(rowdyCrew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(rowdyCrew.getEffectivePower()).isEqualTo(3);
        assertThat(rowdyCrew.getEffectiveToughness()).isEqualTo(3);
    }


    @Test
    @DisplayName("No counters when only 1 card can be discarded (not enough for shared type check)")
    void noCountersWhenOnlyOneCardDiscarded() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Deck has only 1 card — draws 1, discards 1 (can't discard 2)
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new RowdyCrew()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());

        Permanent rowdyCrew = findPermanent(player1, "Rowdy Crew");

        // Only 1 discarded — condition "two cards that share a card type" not met
        assertThat(rowdyCrew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }


    @Test
    @DisplayName("No counters and no discard when deck is empty (no cards drawn)")
    void noDrawOrDiscardWithEmptyDeck() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Empty deck — draws 0
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new RowdyCrew()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());

        Permanent rowdyCrew = findPermanent(player1, "Rowdy Crew");

        assertThat(rowdyCrew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // No random discard logs
        long randomDiscardLogs = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(0);
    }


    @Test
    @DisplayName("Gets counters when both discarded cards are instants")
    void getsCountersWhenBothInstants() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Deck has exactly 2 instants — draws 2 (deck runs out), discards both.
        // Both are instants → shared type → counters.
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));

        harness.setHand(player1, List.of(new RowdyCrew()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());

        Permanent rowdyCrew = findPermanent(player1, "Rowdy Crew");

        assertThat(rowdyCrew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }


    @Test
    @DisplayName("Opponent's hand and graveyard are not affected")
    void opponentNotAffected() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        int opponentGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        harness.setHand(player1, List.of(new RowdyCrew()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardSize);
    }


    @Test
    @DisplayName("Draw replacement choices finish before random discard and counters")
    void completesDrawChoicesBeforeDiscarding() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new Abundance());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RowdyCrew()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        for (int i = 0; i < 3; i++) {
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Rowdy Crew").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }
}
