package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatalystStone.class, CallOfTheHerd.class, ChatterOfTheSquirrel.class})
class CatalystStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces your flashback cost by {2}")
    void reducesOwnFlashbackCost() {
        harness.addToBattlefield(player1, new CatalystStone());
        harness.setGraveyard(player1, List.of(new CallOfTheHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Increases an opponent's flashback cost by {2}")
    void increasesOpponentFlashbackCost() {
        harness.addToBattlefield(player1, new CatalystStone());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player2, List.of(new CallOfTheHerd()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFlashback(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce the normal cost of a card with flashback")
    void doesNotReduceNormalCastCost() {
        harness.addToBattlefield(player1, new CatalystStone());
        harness.setHand(player1, List.of(new CallOfTheHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Reduces only the generic portion when less than {2} remains")
    void doesNotReduceColoredFlashbackCostBelowZeroGenericMana() {
        harness.addToBattlefield(player1, new CatalystStone());
        harness.setGraveyard(player1, List.of(new ChatterOfTheSquirrel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
