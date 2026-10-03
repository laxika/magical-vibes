package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CruelEntertainment.class)
class CruelEntertainmentTest extends BaseCardTest {

    @Test
    @DisplayName("Schedules each target to control the other's next turn")
    void schedulesBothTurnControls() {
        harness.setHand(player1, List.of(new CruelEntertainment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.pendingTurnControl)
                .containsEntry(player1.getId(), player2.getId())
                .containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("Requires two different player targets")
    void cannotTargetSamePlayerTwice() {
        harness.setHand(player1, List.of(new CruelEntertainment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
