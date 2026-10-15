package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrineGiant.class, OmenOfTheSea.class, AltarOfThePantheon.class, NyxbornColossus.class})
class BrineGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for enchantments reduces the generic mana cost")
    void affinityForEnchantmentsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new OmenOfTheSea());
        }
        harness.setHand(player1, List.of(new BrineGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only enchantments controlled by the spell's controller")
    void affinityCountsOnlyControlledEnchantments() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new OmenOfTheSea());
        }
        harness.setHand(player1, List.of(new BrineGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity does not count non-enchantments")
    void affinityDoesNotCountNonEnchantments() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new AltarOfThePantheon());
        }
        harness.setHand(player1, List.of(new BrineGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Each enchantment reduces the generic cost by exactly one")
    void partialAffinityReducesCostExactly() {
        harness.addToBattlefield(player1, new OmenOfTheSea());
        harness.addToBattlefield(player1, new OmenOfTheSea());
        harness.setHand(player1, List.of(new BrineGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Excess enchantments do not remove the blue mana requirement")
    void excessAffinityStillRequiresBlueMana() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new OmenOfTheSea());
        }
        harness.setHand(player1, List.of(new BrineGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Excess enchantments allow casting for one blue mana")
    void excessAffinityStopsAtZeroGenericCost() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new OmenOfTheSea());
        }
        harness.setHand(player1, List.of(new BrineGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brine Giant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Enchantment creatures count toward affinity")
    void enchantmentCreaturesCountTowardAffinity() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new NyxbornColossus());
        }
        harness.setHand(player1, List.of(new BrineGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Enchantments in hand and graveyard do not reduce the cost")
    void enchantmentsOutsideBattlefieldDoNotCount() {
        harness.setHand(player1, List.of(new BrineGiant(), new OmenOfTheSea(),
                new OmenOfTheSea(), new OmenOfTheSea()));
        harness.setGraveyard(player1, List.of(new OmenOfTheSea(),
                new OmenOfTheSea(), new OmenOfTheSea()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
