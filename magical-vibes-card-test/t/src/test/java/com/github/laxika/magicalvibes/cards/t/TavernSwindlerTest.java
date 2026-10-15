package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TavernSwindler.class})
class TavernSwindlerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating pays 3 life and gains 6 only on a won flip")
    void activatingPaysThreeLifeAndGainsSixOnWin() {
        addCreatureReady(player1, new TavernSwindler());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean won = gameLogContains("wins the coin flip");

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

    @Test
    @DisplayName("Life and tap costs are paid before the coin flip resolves")
    void costsArePaidBeforeResolution() {
        addCreatureReady(player1, new TavernSwindler());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 17);
        assertThat(findPermanent(player1, "Tavern Swindler").isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gameLogContains("wins the coin flip")).isFalse();
        assertThat(gameLogContains("loses the coin flip")).isFalse();

        harness.passBothPriorities();

        harness.assertLife(player1, gameLogContains("wins the coin flip") ? 23 : 17);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Swindler cannot activate or pay life again")
    void tappedSwindlerCannotActivate() {
        addCreatureReady(player1, new TavernSwindler()).tap();
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning sick Swindler cannot activate its tap ability")
    void summoningSickSwindlerCannotActivate() {
        addCreatureReady(player1, new TavernSwindler()).setSummoningSick(true);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertLife(player1, 20);
        assertThat(findPermanent(player1, "Tavern Swindler").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
