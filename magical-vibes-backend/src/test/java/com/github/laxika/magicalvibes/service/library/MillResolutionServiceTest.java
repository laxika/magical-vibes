package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.cards.d.DreambornMuse;
import com.github.laxika.magicalvibes.cards.g.Grindclock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Traumatize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MillResolutionServiceTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // =========================================================================
    // resolveMillByHandSize (via DreambornMuse)
    // =========================================================================

    @Nested
    @DisplayName("resolveMillByHandSize")
    class ResolveMillByHandSize {

        @Test
        @DisplayName("Mills cards equal to target player's hand size")
        void millsByHandSize() {
            harness.addToBattlefield(player1, new DreambornMuse());
            harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        }

        @Test
        @DisplayName("Mills nothing when hand is empty")
        void millsNothingWithEmptyHand() {
            harness.addToBattlefield(player1, new DreambornMuse());
            harness.setHand(player1, List.of());
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("mills nothing"));
        }

        @Test
        @DisplayName("Mills only remaining cards when hand size exceeds library")
        void cappedByDeckSize() {
            harness.addToBattlefield(player1, new DreambornMuse());
            harness.setHand(player1, List.of(
                    new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                    new GrizzlyBears(), new GrizzlyBears()));

            List<Card> deck = gd.playerDecks.get(player1.getId());
            while (deck.size() > 2) {
                deck.removeFirst();
            }

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        }
    }

    // =========================================================================
    // resolveMillTargetPlayerByChargeCounters (via Grindclock)
    // =========================================================================

    @Nested
    @DisplayName("resolveMillTargetPlayerByChargeCounters")
    class ResolveMillTargetPlayerByChargeCounters {

        @Test
        @DisplayName("Mills nothing with zero charge counters")
        void millsNothingWithZeroCounters() {
            Permanent grindclock = addReadyPermanent(player1, new Grindclock());
            List<Card> deck = gd.playerDecks.get(player2.getId());
            int deckSizeBefore = deck.size();

            harness.activateAbility(player1, 0, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("mills 0 cards"));
        }

        @Test
        @DisplayName("Mills capped by library size when counters exceed cards")
        void cappedByLibrarySize() {
            Permanent grindclock = addReadyPermanent(player1, new Grindclock());
            grindclock.setChargeCounters(10);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 3) {
                deck.removeFirst();
            }

            harness.activateAbility(player1, 0, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        }
    }

    // =========================================================================
    // resolveMillHalfLibrary (via Traumatize)
    // =========================================================================

    @Nested
    @DisplayName("resolveMillHalfLibrary")
    class ResolveMillHalfLibrary {

        @Test
        @DisplayName("Mills half of library rounded down")
        void millsHalfRoundedDown() {
            harness.setHand(player1, List.of(new Traumatize()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 11) {
                deck.removeFirst();
            }

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        }

        @Test
        @DisplayName("Empty library does nothing")
        void emptyLibraryDoesNothing() {
            harness.setHand(player1, List.of(new Traumatize()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            gd.playerDecks.get(player2.getId()).clear();

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("mills nothing"));
        }
    }
}
