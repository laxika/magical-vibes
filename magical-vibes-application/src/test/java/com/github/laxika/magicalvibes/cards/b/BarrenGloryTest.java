package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BarrenGlory.class)
class BarrenGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game during upkeep with no other permanents and an empty hand")
    void winsWithNoOtherPermanentsAndEmptyHand() {
        harness.addToBattlefield(player1, new BarrenGlory());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger with another permanent or a card in hand")
    void doesNotTriggerWhenConditionIsNotMet() {
        harness.addToBattlefield(player1, new BarrenGlory());
        harness.addToBattlefield(player1, new BarrenGlory());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.setHand(player1, List.of(new BarrenGlory()));
        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Checks both conditions again when the trigger resolves")
    void checksConditionAgainOnResolution() {
        harness.addToBattlefield(player1, new BarrenGlory());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BarrenGlory()));
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
