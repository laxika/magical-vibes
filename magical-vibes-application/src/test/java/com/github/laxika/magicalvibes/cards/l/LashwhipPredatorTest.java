package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LashwhipPredator.class, GrizzlyBears.class})
class LashwhipPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Lashwhip Predator for the reduced cost when an opponent controls three creatures")
    void canCastAtOpponentCreatureThreshold() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOpponentCreatures(3);
        addReducedCostMana();

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast Lashwhip Predator for the reduced cost when an opponent controls only two creatures")
    void cannotCastBelowOpponentCreatureThreshold() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOpponentCreatures(2);
        addReducedCostMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Your own creatures do not count toward the cost reduction")
    void ownCreaturesDoNotEnableReduction() {
        harness.setHand(player1, List.of(new LashwhipPredator()));
        addOwnCreatures(3);
        addReducedCostMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void addOpponentCreatures(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
    }

    private void addOwnCreatures(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
    }

    private void addReducedCostMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
