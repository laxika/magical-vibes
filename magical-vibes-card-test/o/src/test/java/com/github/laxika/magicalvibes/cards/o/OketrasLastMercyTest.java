package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HopeTender;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OketrasLastMercy.class, Plains.class, Island.class, HopeTender.class})
class OketrasLastMercyTest extends BaseCardTest {

    @Nested
    @DisplayName("Life total becomes starting life total")
    @CardUsed({OketrasLastMercy.class})
    class LifeTotal {

        @Test
        @DisplayName("Uses the Commander starting life total")
        void usesCommanderStartingLife() {
            gd.format = DeckFormat.COMMANDER;
            harness.setLife(player1, 4);

            cast();

            harness.assertLife(player1, 40);
        }

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

    @Nested
    @DisplayName("Lands you control don't untap during your next untap step")
    @CardUsed({OketrasLastMercy.class, Plains.class, Island.class, HopeTender.class})
    class LandsDontUntap {

        @Test
        @DisplayName("All of the controller's lands stay tapped during the next untap step")
        void allControllerLandsStayTapped() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
            plains.tap();
            island.tap();

            cast();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(plains.isTapped()).isTrue();
            assertThat(island.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Lands entering after resolution also stay tapped during the next untap step")
        void laterArrivingLandsStayTapped() {
            cast();

            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            plains.tap();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(plains.isTapped()).isTrue();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(plains.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Lands that were untapped at resolution but tapped later stay tapped")
        void landsTappedAfterResolutionStayTapped() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

            cast();
            plains.tap();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(plains.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Multiple copies restrict only the same next untap step")
        void multipleCopiesWearOffTogether() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            plains.tap();

            cast();
            cast();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);
            assertThat(plains.isTapped()).isTrue();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);
            assertThat(plains.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Lands can still untap outside the untap step")
        void abilityCanUntapLandBeforeNextUntapStep() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            Permanent tender = harness.addToBattlefieldAndReturn(player1, new HopeTender());
            tender.setSummoningSick(false);
            plains.tap();

            cast();
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.activateAbility(player1, 1, 0, null, plains.getId());
            harness.passBothPriorities();
            assertThat(plains.isTapped()).isFalse();

            plains.tap();
            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(plains.isTapped()).isTrue();
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
            Permanent tender = harness.addToBattlefieldAndReturn(player1, new HopeTender());
            tender.setSummoningSick(false);
            plains.tap();
            tender.tap();

            cast();

            assertThat(tender.getSkipUntapCount()).isZero();

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            // Land stays tapped, creature untaps
            assertThat(plains.isTapped()).isTrue();
            assertThat(tender.isTapped()).isFalse();
        }

        @Test
        @DisplayName("A land transferred to the opponent is not restricted during their untap step")
        void transferredLandUntapsForOpponent() {
            Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
            plains.tap();

            cast();
            gd.playerBattlefields.get(player1.getId()).remove(plains);
            gd.playerBattlefields.get(player2.getId()).add(plains);

            advanceToNextTurn(player1);

            assertThat(plains.isTapped()).isFalse();
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

    private void cast() {
        harness.setHand(player1, List.of(new OketrasLastMercy()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(currentActivePlayer.equals(player1) ? player2 : player1, TurnStep.UPKEEP);
    }
}
