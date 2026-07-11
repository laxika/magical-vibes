package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WeirdingShaman;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoskBanneretTest extends BaseCardTest {

    // ===== Treefolk cost reduction =====

    @Test
    @DisplayName("Treefolk spells cost {1} less with Bosk Banneret on the battlefield")
    void treefolkSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Battlewand Oak (Treefolk Warrior) costs {2}{G} — with {1} reduction it costs {1}{G}
        harness.setHand(player1, List.of(new BattlewandOak()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Battlewand Oak");
    }

    @Test
    @DisplayName("Treefolk spell is not castable when mana falls short of the reduced cost")
    void treefolkNotCastableWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Battlewand Oak reduced to {1}{G}; a single green is not enough
        harness.setHand(player1, List.of(new BattlewandOak()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Shaman cost reduction =====

    @Test
    @DisplayName("Shaman spells cost {1} less with Bosk Banneret on the battlefield")
    void shamanSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Weirding Shaman (Goblin Shaman) costs {1}{B} — with {1} reduction it costs {B}
        harness.setHand(player1, List.of(new WeirdingShaman()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Weirding Shaman");
    }

    // ===== Non-matching spells are not reduced =====

    @Test
    @DisplayName("Non-Treefolk, non-Shaman spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Grizzly Bears (Bear) costs {1}{G} — not reduced; one green is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Stacking =====

    @Test
    @DisplayName("Two Bannerets reduce a Treefolk spell's cost by {2}")
    void twoBanneretsStackReduction() {
        harness.addToBattlefield(player1, new BoskBanneret());
        harness.addToBattlefield(player1, new BoskBanneret());
        // Battlewand Oak {2}{G} — with {2} reduction the cost is {G}
        harness.setHand(player1, List.of(new BattlewandOak()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Battlewand Oak");
    }

    // ===== Opponent not affected =====

    @Test
    @DisplayName("Bosk Banneret does not reduce opponent's spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Opponent's Weirding Shaman should still cost {1}{B}; one black is not enough
        harness.setHand(player2, List.of(new WeirdingShaman()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
