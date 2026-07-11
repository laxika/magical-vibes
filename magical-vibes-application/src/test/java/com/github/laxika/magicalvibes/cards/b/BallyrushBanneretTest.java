package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KithkinHarbinger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BallyrushBanneretTest extends BaseCardTest {

    // ===== Kithkin cost reduction =====

    @Test
    @DisplayName("Kithkin spells cost {1} less with Ballyrush Banneret on the battlefield")
    void kithkinSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Kithkin Harbinger costs {2}{W} — with {1} reduction it costs {1}{W}, castable with 2 white
        harness.setHand(player1, List.of(new KithkinHarbinger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kithkin Harbinger");
    }

    @Test
    @DisplayName("Kithkin spell is not castable when mana falls short of the reduced cost")
    void kithkinNotCastableWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Kithkin Harbinger reduced to {1}{W}; only one white is not enough
        harness.setHand(player1, List.of(new KithkinHarbinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Soldier cost reduction =====

    @Test
    @DisplayName("Soldier spells cost {1} less with Ballyrush Banneret on the battlefield")
    void soldierSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Field Marshal (Human Soldier) costs {1}{W}{W} — with {1} reduction it costs {W}{W}
        harness.setHand(player1, List.of(new FieldMarshal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Field Marshal");
    }

    // ===== Non-matching spells are not reduced =====

    @Test
    @DisplayName("Non-Kithkin, non-Soldier spells are not reduced")
    void nonMatchingSpellsNotReduced() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Grizzly Bears (Bear) costs {1}{G} — not reduced; one green is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Stacking =====

    @Test
    @DisplayName("Two Bannerets reduce a Kithkin spell's cost by {2}")
    void twoBanneretsStackReduction() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Kithkin Harbinger {2}{W} — with {2} reduction the generic is fully removed, cost is {W}
        harness.setHand(player1, List.of(new KithkinHarbinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Kithkin Harbinger");
    }

    // ===== Opponent not affected =====

    @Test
    @DisplayName("Ballyrush Banneret does not reduce opponent's spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        // Opponent's Kithkin Harbinger should still cost {2}{W}; two white is not enough
        harness.setHand(player2, List.of(new KithkinHarbinger()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
