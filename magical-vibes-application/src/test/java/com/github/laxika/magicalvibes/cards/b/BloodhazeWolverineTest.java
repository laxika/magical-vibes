package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodhazeWolverine.class, GrizzlyBears.class})
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
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

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
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
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
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(wolverine.getPowerModifier()).isZero();
        assertThat(wolverine.getToughnessModifier()).isZero();
        assertThat(wolverine.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private void addCardsToDeck(int count) {
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        }
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }
}
