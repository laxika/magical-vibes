package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

class OketrasLastMercyTest extends BaseCardTest {

    // ===== Life total =====

    @Nested
    @DisplayName("Life total becomes starting life total")
    class LifeTotal {

        @Test
        @DisplayName("Lowers the controller's life total down to their starting life total")
        void lowersLifeToStarting() {
            harness.setLife(player1, 35);

            cast();

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Raises the controller's life total up to their starting life total")
        void raisesLifeToStarting() {
            harness.setLife(player1, 4);

            cast();

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Only the controller's life total changes, not the opponent's")
        void doesNotChangeOpponentLife() {
            harness.setLife(player1, 3);
            harness.setLife(player2, 8);

            cast();

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
        }
    }

    // ===== Lands don't untap =====

    @Nested
    @DisplayName("Lands you control don't untap during your next untap step")
    class LandsDontUntap {

        @Test
        @DisplayName("Marks each of the controller's lands to skip their next untap")
        void marksControllerLands() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
            plains.tap();
            island.tap();

            cast();

            assertThat(plains.getSkipUntapCount()).isEqualTo(1);
            assertThat(island.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Controller's lands stay tapped through their next untap step")
        void landsStayTappedNextUntapStep() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            plains.tap();

            cast();

            // player1's turn -> player2's untap -> player1's next untap
            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(plains.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Controller's lands untap normally on the following turn")
        void landsUntapFollowingTurn() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            plains.tap();

            cast();

            // Reach player1's next untap step (skip consumed, still tapped)
            advanceToNextTurn(player1);
            advanceToNextTurn(player2);
            assertThat(plains.isTapped()).isTrue();

            // Reach the untap step after that — untaps normally now
            advanceToNextTurn(player1);
            advanceToNextTurn(player2);
            assertThat(plains.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Non-land permanents you control untap normally")
        void nonLandUntapsNormally() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            bears.setSummoningSick(false);
            plains.tap();
            bears.tap();

            cast();

            assertThat(bears.getSkipUntapCount()).isZero();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            // Land stays tapped, creature untaps
            assertThat(plains.isTapped()).isTrue();
            assertThat(bears.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Opponent's lands are unaffected")
        void opponentLandsUnaffected() {
            Permanent opponentPlains = harness.addToBattlefieldAndReturn(player2, new Plains());
            opponentPlains.tap();

            cast();

            assertThat(opponentPlains.getSkipUntapCount()).isZero();

            // Reach player2's untap step — opponent's land untaps normally
            advanceToNextTurn(player1);

            assertThat(opponentPlains.isTapped()).isFalse();
        }
    }

    // ===== Helpers =====

    private void cast() {
        harness.setHand(player1, List.of(new OketrasLastMercy()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
