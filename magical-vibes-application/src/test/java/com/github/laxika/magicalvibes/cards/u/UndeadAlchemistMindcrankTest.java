package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mindcrank;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Regression test for Undead Alchemist + Mindcrank combat interaction.
 * Reproduces a NullPointerException found in AI fuzz testing where:
 * 1. Undead Alchemist replaces Zombie combat damage with milling
 * 2. A non-Zombie attacker deals normal combat damage
 * 3. Mindcrank triggers on the life loss, milling more cards
 * 4. Milled creature cards trigger Undead Alchemist again, creating tokens
 */
@CardUsed({UndeadAlchemist.class, Mindcrank.class, GrizzlyBears.class, Shock.class})
class UndeadAlchemistMindcrankTest extends BaseCardTest {

    @Test
    @DisplayName("Undead Alchemist + Mindcrank + non-Zombie attacker: full combat chain completes without error")
    void fullCombatChainDoesNotThrow() {
        // Player 1 has: Undead Alchemist (4/2 Zombie) + Mindcrank + GrizzlyBears (2/2 non-Zombie)
        Permanent alchemist = addCreatureReady(player1, new UndeadAlchemist());
        alchemist.setAttacking(true);

        harness.addToBattlefield(player1, new Mindcrank());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        harness.setLife(player2, 20);

        // Set up player2's deck with creature cards to trigger Undead Alchemist
        harness.setLibrary(player2, IntStream.range(0, 20)
                .mapToObj(i -> new GrizzlyBears()).toList());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // This should NOT throw NPE
        assertThatCode(() -> harness.passBothPriorities())
                .doesNotThrowAnyException();

        // Undead Alchemist's 4 damage replaced with mill 4 → no life loss from that
        // GrizzlyBears deals 2 combat damage → player2 loses 2 life
        // Mindcrank triggers → player2 mills 2 more cards
        // Both mill waves trigger Undead Alchemist for each creature card milled

        // Player2 should have lost exactly 2 life (from GrizzlyBears only)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Zombie tokens should have been created
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Undead Alchemist + Mindcrank with many creature cards in deck")
    void manyCreatureCardsInDeck() {
        // Same setup but with a large deck of creatures to stress the recursive milling
        Permanent alchemist = addCreatureReady(player1, new UndeadAlchemist());
        alchemist.setAttacking(true);

        harness.addToBattlefield(player1, new Mindcrank());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        harness.setLife(player2, 20);

        // Larger deck with mix of creature and non-creature cards
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            if (i % 3 == 0) {
                library.add(new Shock());
            } else {
                library.add(new GrizzlyBears());
            }
        }
        harness.setLibrary(player2, library);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatCode(() -> harness.passBothPriorities())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Undead Alchemist + Mindcrank: deck runs out during mill chain")
    void deckRunsOutDuringMillChain() {
        Permanent alchemist = addCreatureReady(player1, new UndeadAlchemist());
        alchemist.setAttacking(true);

        harness.addToBattlefield(player1, new Mindcrank());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        harness.setLife(player2, 20);

        // Small deck that will run out during milling
        harness.setLibrary(player2, IntStream.range(0, 5)
                .mapToObj(i -> new GrizzlyBears()).toList());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatCode(() -> harness.passBothPriorities())
                .doesNotThrowAnyException();

        // Deck should be empty after the mill chain
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
