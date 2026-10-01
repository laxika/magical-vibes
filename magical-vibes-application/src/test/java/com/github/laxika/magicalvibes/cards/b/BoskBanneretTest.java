package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.l.LeafCrownedElder;
import com.github.laxika.magicalvibes.cards.w.WeirdingShaman;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoskBanneret.class, IndomitableAncients.class, WeirdingShaman.class,
        ElvishWarrior.class, LeafCrownedElder.class})
class BoskBanneretTest extends BaseCardTest {

    // ===== Treefolk cost reduction =====

    @Test
    @DisplayName("Treefolk spells cost {1} less with Bosk Banneret on the battlefield")
    void treefolkSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Indomitable Ancients (Treefolk Warrior) costs {2}{W}{W} — with {1} reduction it costs {1}{W}{W}
        harness.castFromHand(player1, new IndomitableAncients(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Indomitable Ancients");
    }

    @Test
    @DisplayName("Treefolk spell is not castable when mana falls short of the reduced cost")
    void treefolkNotCastableWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Indomitable Ancients is reduced to {1}{W}{W}; two white mana are not enough
        harness.setHand(player1, List.of(new IndomitableAncients()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Treefolk Shaman receives only one reduction")
    void overlappingSubtypesReceiveOnlyOneReduction() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Leaf-Crowned Elder (Treefolk Shaman) costs {2}{G}{G}; one reduction still leaves {1}{G}{G}
        harness.setHand(player1, List.of(new LeafCrownedElder()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Shaman cost reduction =====

    @Test
    @DisplayName("Shaman spells cost {1} less with Bosk Banneret on the battlefield")
    void shamanSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Weirding Shaman (Goblin Shaman) costs {1}{B} — with {1} reduction it costs {B}
        harness.castFromHand(player1, new WeirdingShaman(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Weirding Shaman");
    }

    // ===== Non-matching spells are not reduced =====

    @Test
    @DisplayName("Non-Treefolk, non-Shaman spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Elvish Warrior (Elf Warrior) costs {G}{G} — not reduced; one green is not enough
        harness.setHand(player1, List.of(new ElvishWarrior()));
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
        // Indomitable Ancients {2}{W}{W} — with {2} reduction the cost is {W}{W}
        harness.castFromHand(player1, new IndomitableAncients(), "{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Indomitable Ancients");
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
