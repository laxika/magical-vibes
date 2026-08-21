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
}
