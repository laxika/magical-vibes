package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

class BontusLastReckoningTest extends BaseCardTest {

    // ===== Destroy all creatures =====

    @Nested
    @DisplayName("Destroy all creatures")
    class DestroyAllCreatures {

        @Test
        @DisplayName("Destroys all creatures on both sides")
        void destroysAllCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new LlanowarElves());

            cast();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        }

        @Test
        @DisplayName("Leaves lands on the battlefield")
        void leavesLands() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new Swamp());

            cast();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Swamp"));
        }
    }

    // ===== Lands you control don't untap =====

    @Nested
    @DisplayName("Lands you control don't untap during your next untap step")
    class LandsDontUntap {

        @Test
        @DisplayName("Marks each of the controller's lands to skip their next untap")
        void marksControllerLands() {
            Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
            swamp.tap();

            cast();

            assertThat(swamp.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Controller's lands stay tapped through their next untap step, untap the turn after")
        void landsStayTappedThenUntap() {
            Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
            swamp.tap();

            cast();

            // player1's turn -> player2's untap -> player1's next untap (skip consumed)
            advanceToNextTurn(player1);
            advanceToNextTurn(player2);
            assertThat(swamp.isTapped()).isTrue();

            // Following untap step untaps normally
            advanceToNextTurn(player1);
            advanceToNextTurn(player2);
            assertThat(swamp.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Opponent's lands are unaffected")
        void opponentLandsUnaffected() {
            Permanent opponentSwamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
            opponentSwamp.tap();

            cast();

            assertThat(opponentSwamp.getSkipUntapCount()).isZero();

            // Reach player2's untap step — opponent's land untaps normally
            advanceToNextTurn(player1);

            assertThat(opponentSwamp.isTapped()).isFalse();
        }
    }

    // ===== Helpers =====

    private void cast() {
        harness.setHand(player1, List.of(new BontusLastReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 3);
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
