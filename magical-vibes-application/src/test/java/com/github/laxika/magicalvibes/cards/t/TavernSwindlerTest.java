package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TavernSwindlerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating pays 3 life and gains 6 only on a won flip")
    void activatingPaysThreeLifeAndGainsSixOnWin() {
        addCreatureReady(player1, new TavernSwindler());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip"));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(won ? 23 : 17);
        assertThat(findPermanent(player1, "Tavern Swindler").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough life to pay the cost")
    void cannotActivateWithoutEnoughLife() {
        addCreatureReady(player1, new TavernSwindler());
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(findPermanent(player1, "Tavern Swindler").isTapped()).isFalse();
    }
}
