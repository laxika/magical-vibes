package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLordOfTheEagles.class, SuntailHawk.class, GrizzlyBears.class})
class TheLordOfTheEaglesTest extends BaseCardTest {

    @Test
    @DisplayName("Costs full generic mana with no flying creatures")
    void costsFullAmountWithoutFlyingCreatures() {
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Reduces its generic cost by the total power of flying creatures")
    void reducesCostByFlyingCreaturePower() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Nonflying creature power does not reduce the cost")
    void ignoresNonflyingCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent's flying creatures do not reduce the cost")
    void ignoresOpponentsFlyingCreatures() {
        harness.addToBattlefield(player2, new TheLordOfTheEagles());
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Reduction beyond seven still leaves the two blue mana requirement")
    void excessiveReductionLeavesColoredCost() {
        harness.addToBattlefield(player1, new TheLordOfTheEagles());
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess reduction cannot pay blue mana with colorless mana")
    void reductionCannotReplaceBlueMana() {
        harness.addToBattlefield(player1, new TheLordOfTheEagles());
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flash permits casting during the opponent's upkeep")
    void castsDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Lord of the Eagles");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The reduction uses current power including counters")
    void usesCurrentPower() {
        var flyer = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        flyer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A flyer with negative power subtracts from the total")
    void negativePowerSubtractsFromTotal() {
        var flyer = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        flyer.setPowerModifier(-2);
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new TheLordOfTheEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
