package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodhazeWolverine.class, Mountain.class})
class BloodhazeWolverineTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn gives Bloodhaze Wolverine +1/+1 and first strike")
    void secondDrawGivesBoostAndFirstStrike() {
        Permanent wolverine = harness.addToBattlefieldAndReturn(player1, new BloodhazeWolverine());
        addCardsToDeck(3);

        draw(player1.getId());
        assertThat(wolverine.getPowerModifier()).isZero();
        assertThat(wolverine.getToughnessModifier()).isZero();
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        draw(player1.getId());
        resolveAllTriggers();

        assertThat(wolverine.getPowerModifier()).isEqualTo(1);
        assertThat(wolverine.getToughnessModifier()).isEqualTo(1);
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability does not trigger again on the third card drawn that turn")
    void triggersOnlyOnSecondDraw() {
        Permanent wolverine = harness.addToBattlefieldAndReturn(player1, new BloodhazeWolverine());
        addCardsToDeck(3);

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();
        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(wolverine.getPowerModifier()).isEqualTo(1);
        assertThat(wolverine.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost and first strike wear off at end of turn")
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        Permanent wolverine = harness.addToBattlefieldAndReturn(player1, new BloodhazeWolverine());
        addCardsToDeck(2);

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(wolverine.getPowerModifier()).isZero();
        assertThat(wolverine.getToughnessModifier()).isZero();
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The controller's second draw triggers during the opponent's turn")
    void triggersDuringOpponentsTurn() {
        Permanent wolverine = harness.addToBattlefieldAndReturn(player1, new BloodhazeWolverine());
        addCardsToDeck(2);
        gd.activePlayerId = player2.getId();

        draw(player1.getId());
        draw(player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(wolverine.getPowerModifier()).isZero();
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        resolveAllTriggers();

        assertThat(wolverine.getPowerModifier()).isEqualTo(1);
        assertThat(wolverine.getToughnessModifier()).isEqualTo(1);
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent draws do not trigger the ability or count toward the controller's second draw")
    void drawCountsAreIndependentForEachPlayer() {
        Permanent wolverine = harness.addToBattlefieldAndReturn(player1, new BloodhazeWolverine());
        addCardsToDeck(2);
        harness.setLibrary(player2, java.util.List.of(new Mountain(), new Mountain()));

        draw(player2.getId());
        draw(player2.getId());
        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(wolverine.getPowerModifier()).isZero();
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        draw(player1.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(wolverine.getPowerModifier()).isEqualTo(1);
        assertThat(wolverine.getToughnessModifier()).isEqualTo(1);
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Drawing before Wolverine enters still counts toward the second draw")
    void firstDrawBeforeEnteringCounts() {
        addCardsToDeck(2);
        draw(player1.getId());
        Permanent wolverine = harness.addToBattlefieldAndReturn(player1, new BloodhazeWolverine());

        draw(player1.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(wolverine.getPowerModifier()).isEqualTo(1);
        assertThat(wolverine.getToughnessModifier()).isEqualTo(1);
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on a later draw")
    void enteringAfterSecondDrawDoesNotTrigger() {
        addCardsToDeck(3);
        draw(player1.getId());
        draw(player1.getId());
        Permanent wolverine = harness.addToBattlefieldAndReturn(player1, new BloodhazeWolverine());

        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(wolverine.getPowerModifier()).isZero();
        assertThat(wolverine.getToughnessModifier()).isZero();
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private void addCardsToDeck(int count) {
        harness.setLibrary(player1, IntStream.range(0, count).mapToObj(i -> new Mountain()).toList());
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }
}
