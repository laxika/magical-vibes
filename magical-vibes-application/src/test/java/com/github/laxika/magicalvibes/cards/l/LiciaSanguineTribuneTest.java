package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LiciaSanguineTribune.class)
class LiciaSanguineTribuneTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less to cast for each life gained by its controller this turn")
    void costReductionUsesControllersLifeGain() {
        gd.lifeGainedThisTurn.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new LiciaSanguineTribune()));
        addColoredMana(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent life gain does not reduce the casting cost")
    void costReductionIgnoresOpponentsLifeGain() {
        gd.lifeGainedThisTurn.put(player2.getId(), 4);
        harness.setHand(player1, List.of(new LiciaSanguineTribune()));
        addColoredMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Paying 5 life puts three +1/+1 counters on Licia")
    void payingLifePutsCountersOnLicia() {
        Permanent licia = addReadyLicia(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(licia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Licia's ability can be activated only once each turn")
    void abilityCanBeActivatedOnlyOnceEachTurn() {
        addReadyLicia(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Licia's ability cannot be activated during an opponent's turn")
    void abilityCannotBeActivatedDuringOpponentsTurn() {
        addReadyLicia(player1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent addReadyLicia(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new LiciaSanguineTribune());
    }

    private void addColoredMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
