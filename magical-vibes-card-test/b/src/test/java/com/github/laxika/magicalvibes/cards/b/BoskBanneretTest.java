package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.l.LeafCrownedElder;
import com.github.laxika.magicalvibes.cards.r.ReachOfBranches;
import com.github.laxika.magicalvibes.cards.w.WeirdingShaman;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoskBanneret.class, IndomitableAncients.class, WeirdingShaman.class,
        ElvishWarrior.class, LeafCrownedElder.class, BramblewoodParagon.class, ReachOfBranches.class})
class BoskBanneretTest extends BaseCardTest {

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
        assertThatThrownBy(() -> harness.castFromHand(player1, new IndomitableAncients(), "{W}{W}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Treefolk Shaman receives only one reduction")
    void overlappingSubtypesReceiveOnlyOneReduction() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Leaf-Crowned Elder (Treefolk Shaman) costs {2}{G}{G}; one reduction still leaves {1}{G}{G}
        assertThatThrownBy(() -> harness.castFromHand(player1, new LeafCrownedElder(), "{G}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shaman spells cost {1} less with Bosk Banneret on the battlefield")
    void shamanSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Weirding Shaman (Goblin Shaman) costs {1}{B} — with {1} reduction it costs {B}
        harness.castFromHand(player1, new WeirdingShaman(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Weirding Shaman");
    }

    @Test
    @DisplayName("Non-Treefolk, non-Shaman spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Elvish Warrior (Elf Warrior) costs {G}{G} — not reduced; one green is not enough
        assertThatThrownBy(() -> harness.castFromHand(player1, new ElvishWarrior(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

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

    @Test
    @DisplayName("Bosk Banneret does not reduce opponent's spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new BoskBanneret());
        // Opponent's Weirding Shaman should still cost {1}{B}; one black is not enough
        assertThatThrownBy(() -> harness.castFromHand(player2, new WeirdingShaman(), "{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void overlappingSubtypesCanBeCastWithOneReduction() {
        harness.addToBattlefield(player1, new BoskBanneret());
        harness.castFromHand(player1, new LeafCrownedElder(), "{1}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(LeafCrownedElder.class);
    }

    @Test
    void nonmatchingGenericCostIsNotReduced() {
        harness.addToBattlefield(player1, new BoskBanneret());

        assertThatThrownBy(() -> harness.castFromHand(player1, new BramblewoodParagon(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kindredTreefolkInstantCostsOneLess() {
        harness.addToBattlefield(player1, new BoskBanneret());
        harness.castFromHand(player1, new ReachOfBranches(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(ReachOfBranches.class);
    }

    @Test
    void excessReductionDoesNotRemoveColoredMana() {
        harness.addToBattlefield(player1, new BoskBanneret());
        harness.addToBattlefield(player1, new BoskBanneret());
        harness.addToBattlefield(player1, new BoskBanneret());

        assertThatThrownBy(() -> harness.castFromHand(player1, new IndomitableAncients(), "{W}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void banneretInHandDoesNotReduceItsOwnCost() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new BoskBanneret(), "{G}"))
                .isInstanceOf(IllegalStateException.class);
    }
}
