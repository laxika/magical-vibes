package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GeistOfSaintTraft;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JhoirasFamiliarTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Jhoira's Familiar has correct effects")
    void hasCorrectEffects() {
        JhoirasFamiliar card = new JhoirasFamiliar();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(ReduceCastCostForMatchingSpellsEffect.class);

        ReduceCastCostForMatchingSpellsEffect effect = (ReduceCastCostForMatchingSpellsEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(effect.predicate()).isInstanceOf(CardIsHistoricPredicate.class);
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.scope()).isEqualTo(CostModificationScope.SELF);
    }

    // ===== Cost reduction: artifact spells =====

    @Test
    @DisplayName("Artifact spells cost {1} less to cast")
    void artifactSpellsCostOneLess() {
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        // Jhoira's Familiar costs {4} — with {1} reduction it should cost {3}
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Jhoira's Familiar");
    }

    @Test
    @DisplayName("Cannot cast artifact without enough mana even with cost reduction")
    void cannotCastArtifactWithoutEnoughMana() {
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        // Jhoira's Familiar costs {4} — with {1} reduction it needs {3}; only {2} is not enough
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cost reduction: legendary spells =====

    @Test
    @DisplayName("Legendary spells cost {1} less to cast")
    void legendarySpellsCostOneLess() {
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        // Geist of Saint Traft costs {1}{W}{U} — with {1} reduction it should cost {W}{U}
        harness.setHand(player1, List.of(new GeistOfSaintTraft()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Geist of Saint Traft");
    }

    // ===== Cost reduction: Saga spells =====

    @Test
    @DisplayName("Saga spells cost {1} less to cast")
    void sagaSpellsCostOneLess() {
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        // History of Benalia costs {1}{W}{W} — with {1} reduction it should cost {W}{W}
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("History of Benalia");
    }

    // ===== Non-historic spells are not affected =====

    @Test
    @DisplayName("Non-historic creature spells are not affected by cost reduction")
    void nonHistoricCreatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        // Grizzly Bears costs {1}{G} — should still cost {1}{G}, not reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only 1 green mana — not enough for {1}{G} since non-historic spells are not affected
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Multiple Familiars stack =====

    @Test
    @DisplayName("Two Jhoira's Familiars reduce historic spell cost by {2}")
    void twoFamiliarsStackReduction() {
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        // Jhoira's Familiar costs {4} — with {2} reduction it should cost {2}
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Jhoira's Familiar");
    }

    // ===== Opponent's spells are not affected =====

    @Test
    @DisplayName("Opponent's historic spells are not affected by cost reduction")
    void opponentHistoricSpellsNotReduced() {
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        // Opponent's Jhoira's Familiar should still cost {4}
        harness.setHand(player2, List.of(new JhoirasFamiliar()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        // Only 3 colorless mana — not enough for {4} since opponent's spells are not affected
        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
