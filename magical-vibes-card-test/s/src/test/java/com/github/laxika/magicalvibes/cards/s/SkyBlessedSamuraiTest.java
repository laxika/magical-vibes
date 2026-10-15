package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NetworkDisruptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyBlessedSamurai.class, NetworkDisruptor.class})
class SkyBlessedSamuraiTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for enchantments reduces the generic mana cost")
    void affinityForEnchantmentsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new SkyBlessedSamurai());
        }
        harness.castFromHand(player1, new SkyBlessedSamurai(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only enchantments controlled by the spell's controller")
    void affinityCountsOnlyControlledEnchantments() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new SkyBlessedSamurai());
        }
        harness.setHand(player1, List.of(new SkyBlessedSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity does not count non-enchantments")
    void affinityDoesNotCountNonEnchantments() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new NetworkDisruptor());
        }
        harness.setHand(player1, List.of(new SkyBlessedSamurai()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity counts enchantment creatures and reduces only the remaining generic cost")
    void affinityCountsEnchantmentCreatures() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new SkyBlessedSamurai());
        }

        harness.castFromHand(player1, new SkyBlessedSamurai(), "{3}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Affinity cannot remove the white mana requirement even with excess enchantments")
    void affinityCannotReduceColoredCost() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new SkyBlessedSamurai());
        }
        harness.setHand(player1, List.of(new SkyBlessedSamurai()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity stops reducing the cost once all generic mana has been removed")
    void affinityWithExcessEnchantmentsStillCostsWhite() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new SkyBlessedSamurai());
        }

        harness.castFromHand(player1, new SkyBlessedSamurai(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("With no enchantments the spell in hand does not reduce its own cost")
    void spellDoesNotCountItselfInHand() {
        harness.setHand(player1, List.of(new SkyBlessedSamurai()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
