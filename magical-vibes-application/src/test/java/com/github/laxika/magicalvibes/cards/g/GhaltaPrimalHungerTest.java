package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhaltaPrimalHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {10}{G}{G} with no power among creatures you control")
    void costsFullAmountWithoutControlledCreatures() {
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 11);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Costs less by the total power of creatures you control")
    void reducesCostByControlledCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent creatures do not reduce the cost")
    void ignoresOpponentsCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 11);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Negative power subtracts from the total power")
    void negativePowerSubtractsFromTotal() {
        harness.addToBattlefield(player1, testCreature(-1, 5));
        harness.addToBattlefield(player1, testCreature(2, 2));
        harness.setHand(player1, List.of(new GhaltaPrimalHunger()));
        harness.addMana(player1, ManaColor.GREEN, 11);

        harness.castCreature(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    private Card testCreature(int power, int toughness) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
