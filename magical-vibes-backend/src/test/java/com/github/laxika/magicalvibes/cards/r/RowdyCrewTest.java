package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawAndRandomDiscardWithSharedTypeCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RowdyCrewTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has DrawAndRandomDiscardWithSharedTypeCountersEffect(3, 2, 2) on ON_ENTER_BATTLEFIELD")
    void hasCorrectEffect() {
        RowdyCrew card = new RowdyCrew();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DrawAndRandomDiscardWithSharedTypeCountersEffect.class);

        DrawAndRandomDiscardWithSharedTypeCountersEffect effect =
                (DrawAndRandomDiscardWithSharedTypeCountersEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.drawAmount()).isEqualTo(3);
        assertThat(effect.discardAmount()).isEqualTo(2);
        assertThat(effect.counterAmount()).isEqualTo(2);
    }

    // ===== ETB: draw and discard =====

    @Test
    @DisplayName("ETB draws 3 cards and discards 2 at random (net +1 card in hand)")
    void etbDrawsThreeDiscardsTwo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Set up deck with 3 creatures so we know what type is drawn
        setDeck(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, new ArrayList<>(List.of(new RowdyCrew())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → enters battlefield
        harness.passBothPriorities(); // resolve ETB trigger

        // Drew 3, discarded 2 → 1 card in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Deck should be empty (had 3, drew 3)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // 2 random discards should be logged
        long randomDiscardLogs = gd.gameLog.stream()
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(2);
    }

    // ===== Counters: shared card type =====

    @Test
    @DisplayName("Gets +1/+1 counters when discarded cards share a card type (all creatures)")
    void getsCountersWhenDiscardedShareType() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // All 3 drawn cards are creatures — any 2 discarded will share type
        setDeck(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, new ArrayList<>(List.of(new RowdyCrew())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent rowdyCrew = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rowdy Crew"))
                .findFirst().orElseThrow();

        // Should have 2 +1/+1 counters → 5/5
        assertThat(rowdyCrew.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(rowdyCrew.getEffectivePower()).isEqualTo(5);
        assertThat(rowdyCrew.getEffectiveToughness()).isEqualTo(5);
    }

    // ===== Counters: no shared card type =====

    @Test
    @DisplayName("No counters when discarded cards do not share a card type")
    void noCountersWhenDiscardedDontShareType() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Deck has exactly 1 creature and 1 land — draws only 2 (deck runs out),
        // then discards both at random. Creature and land don't share a card type.
        setDeck(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.setHand(player1, new ArrayList<>(List.of(new RowdyCrew())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent rowdyCrew = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rowdy Crew"))
                .findFirst().orElseThrow();

        // No counters — creature and land don't share a card type
        assertThat(rowdyCrew.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(rowdyCrew.getEffectivePower()).isEqualTo(3);
        assertThat(rowdyCrew.getEffectiveToughness()).isEqualTo(3);
    }

    // ===== Edge: only 1 card to discard =====

    @Test
    @DisplayName("No counters when only 1 card can be discarded (not enough for shared type check)")
    void noCountersWhenOnlyOneCardDiscarded() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Deck has only 1 card — draws 1, discards 1 (can't discard 2)
        setDeck(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, new ArrayList<>(List.of(new RowdyCrew())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent rowdyCrew = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rowdy Crew"))
                .findFirst().orElseThrow();

        // Only 1 discarded — condition "two cards that share a card type" not met
        assertThat(rowdyCrew.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Edge: empty deck =====

    @Test
    @DisplayName("No counters and no discard when deck is empty (no cards drawn)")
    void noDrawOrDiscardWithEmptyDeck() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Empty deck — draws 0
        setDeck(player1, List.of());

        harness.setHand(player1, new ArrayList<>(List.of(new RowdyCrew())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent rowdyCrew = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rowdy Crew"))
                .findFirst().orElseThrow();

        assertThat(rowdyCrew.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // No random discard logs
        long randomDiscardLogs = gd.gameLog.stream()
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(0);
    }

    // ===== Counters when both instants =====

    @Test
    @DisplayName("Gets counters when both discarded cards are instants")
    void getsCountersWhenBothInstants() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Deck has exactly 2 instants — draws 2 (deck runs out), discards both.
        // Both are instants → shared type → counters.
        setDeck(player1, List.of(new Shock(), new Shock()));

        harness.setHand(player1, new ArrayList<>(List.of(new RowdyCrew())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent rowdyCrew = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rowdy Crew"))
                .findFirst().orElseThrow();

        assertThat(rowdyCrew.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== Opponent not affected =====

    @Test
    @DisplayName("Opponent's hand and graveyard are not affected")
    void opponentNotAffected() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        setDeck(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        int opponentGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        harness.setHand(player1, new ArrayList<>(List.of(new RowdyCrew())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardSize);
    }

    // ===== Helpers =====

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
