package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Anticipate;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemurBattlecrier.class, AirElemental.class, Anticipate.class, GrizzlyBears.class})
class TemurBattlecrierTest extends BaseCardTest {

    @Test
    @DisplayName("All spells cost {1} less for each creature with power 4 or greater")
    void reducesAllSpellsForEachQualifyingCreature() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Air Elemental");
    }

    @Test
    @DisplayName("Creatures with power less than 4 do not add to the reduction")
    void doesNotCountLowPowerCreatures() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction applies to noncreature spells")
    void reducesNoncreatureSpells() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.setHand(player1, List.of(new Anticipate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Anticipate");
    }

    @Test
    @DisplayName("The reduction applies only during its controller's turn")
    void doesNotReduceDuringOpponentTurn() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.setHand(player1, List.of(new Anticipate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opposing creatures do not contribute to the reduction")
    void doesNotCountOpposingCreatures() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent cannot use Battlecrier's reduction on their own turn")
    void doesNotReduceOpponentsSpells() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.setHand(player2, List.of(new Anticipate()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple Battlecriers each count every qualifying creature")
    void multipleBattlecriersStackTheirReductions() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess generic reduction does not pay colored mana")
    void doesNotReduceColoredMana() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.addToBattlefield(player1, new TemurBattlecrier());
        harness.setHand(player1, List.of(new TemurBattlecrier()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Battlecrier stops counting itself when its power falls below four")
    void usesBattlecriersCurrentPower() {
        var battlecrier = harness.addToBattlefieldAndReturn(player1, new TemurBattlecrier());
        battlecrier.setPowerModifier(-1);
        harness.setHand(player1, List.of(new Anticipate()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature boosted to power four contributes to the reduction")
    void countsCreaturesWithIncreasedPower() {
        harness.addToBattlefield(player1, new TemurBattlecrier());
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setPowerModifier(2);
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
