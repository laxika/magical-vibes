package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArcaneMeleeTest extends BaseCardTest {

    @Test
    @DisplayName("Sorcery spells cost {2} less for the controller")
    void sorceryCostsTwoLessForController() {
        harness.addToBattlefield(player1, new ArcaneMelee());
        // Divination {2}{U} reduced to {U}
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Instant spells cost {2} less for the controller")
    void instantCostsTwoLessForController() {
        harness.addToBattlefield(player1, new ArcaneMelee());
        // Angel's Mercy {2}{W}{W} reduced to {W}{W}
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel's Mercy");
    }

    @Test
    @DisplayName("Reduction is symmetric — opponents' instants and sorceries are cheaper too")
    void opponentSpellsAreAlsoReduced() {
        harness.addToBattlefield(player1, new ArcaneMelee());
        harness.setHand(player2, List.of(new AngelsMercy()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel's Mercy");
    }

    @Test
    @DisplayName("Creature spells are not reduced")
    void creatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new ArcaneMelee());
        // Grizzly Bears {1}{G} is unaffected; one green is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Arcane Melees reduce a sorcery's cost by {4}")
    void reductionsStack() {
        harness.addToBattlefield(player1, new ArcaneMelee());
        harness.addToBattlefield(player1, new ArcaneMelee());
        // Army of the Damned {5}{B}{B}{B} reduced to {1}{B}{B}{B}
        harness.setHand(player1, List.of(new ArmyOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Army of the Damned");
    }

    @Test
    @DisplayName("A single Arcane Melee is not enough for a {5}{B}{B}{B} sorcery on four mana")
    void singleMeleeDoesNotStack() {
        harness.addToBattlefield(player1, new ArcaneMelee());
        // Army of the Damned reduced only to {3}{B}{B}{B}
        harness.setHand(player1, List.of(new ArmyOfTheDamned()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Instant is not castable when mana falls short of the reduced cost")
    void notCastableBelowReducedCost() {
        harness.addToBattlefield(player1, new ArcaneMelee());
        // Reduced to {W}{W}; one white is still not enough
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
