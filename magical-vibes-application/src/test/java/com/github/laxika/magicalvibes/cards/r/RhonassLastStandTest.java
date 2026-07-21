package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

class RhonassLastStandTest extends BaseCardTest {

    @Nested
    @DisplayName("Create a 5/4 green Snake creature token")
    class CreateSnake {

        @Test
        @DisplayName("Creates a 5/4 green Snake under the controller's control")
        void createsSnakeToken() {
            cast();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().getName().equals("Snake"))
                    .hasSize(1)
                    .allSatisfy(p -> {
                        assertThat(p.getCard().isToken()).isTrue();
                        assertThat(p.getCard().getPower()).isEqualTo(5);
                        assertThat(p.getCard().getToughness()).isEqualTo(4);
                        assertThat(p.getCard().getColor()).isEqualTo(CardColor.GREEN);
                        assertThat(p.getCard().getSubtypes()).contains(CardSubtype.SNAKE);
                    });
        }
    }

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

            advanceToNextTurn(player1);
            advanceToNextTurn(player2);

            assertThat(plains.isTapped()).isTrue();
            assertThat(plains.getSkipUntapCount()).isZero();
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

            advanceToNextTurn(player1);

            assertThat(opponentPlains.isTapped()).isFalse();
        }
    }

    private void cast() {
        harness.setHand(player1, List.of(new RhonassLastStand()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
