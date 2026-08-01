package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RakdosLordOfRiotsTest extends BaseCardTest {

    private void addRakdosMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    @Test
    @DisplayName("Not castable when no opponent has lost life this turn")
    void notCastableWithoutOpponentLifeLoss() {
        harness.setHand(player1, List.of(new RakdosLordOfRiots()));
        addRakdosMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Castable after an opponent lost life this turn")
    void castableAfterOpponentLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new RakdosLordOfRiots()));
        addRakdosMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rakdos, Lord of Riots");
    }

    @Test
    @DisplayName("Controller's own life loss does not enable the cast")
    void ownLifeLossDoesNotEnableCast() {
        gd.lifeLostThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new RakdosLordOfRiots()));
        addRakdosMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creature spells cost {1} less per life opponents lost this turn")
    void reducesCreatureSpellCost() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        // Grizzly Bears costs {1}{G} — with {1} reduction it costs just {G}
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Without opponent life loss, creature spells are not reduced")
    void noReductionWithoutLifeLoss() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature spells are not reduced")
    void noncreatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player2.getId(), 5);
        // Shock costs {R}; reduction never eats colored mana, and noncreatures are excluded anyway
        harness.setHand(player1, List.of(new Shock()));
        // Intentionally empty pool — if Shock were free it would cast
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller's own life loss does not reduce creature costs")
    void ownLifeLossDoesNotReduce() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
