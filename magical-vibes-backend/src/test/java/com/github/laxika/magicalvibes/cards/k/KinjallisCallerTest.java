package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KinjallisCallerTest extends BaseCardTest {

    // ===== Card effects =====

    @Test
    @DisplayName("Kinjalli's Caller has the cost reduction effect for Dinosaur subtype")
    void hasCorrectEffects() {
        KinjallisCaller card = new KinjallisCaller();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(ReduceOwnCastCostForSubtypeEffect.class);

        ReduceOwnCastCostForSubtypeEffect effect = (ReduceOwnCastCostForSubtypeEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(effect.affectedSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== Cost reduction =====

    @Test
    @DisplayName("Dinosaur spells cost {1} less to cast with Kinjalli's Caller on the battlefield")
    void dinosaurSpellsCostOneLess() {
        harness.addToBattlefield(player1, new KinjallisCaller());
        // Frenzied Raptor costs {2}{R} — with {1} reduction it should cost {1}{R}
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Frenzied Raptor");
    }

    @Test
    @DisplayName("Cannot cast Dinosaur spell without enough mana even with cost reduction")
    void cannotCastDinosaurWithoutEnoughMana() {
        harness.addToBattlefield(player1, new KinjallisCaller());
        // Frenzied Raptor costs {2}{R} — with {1} reduction needs {1}{R}; only {R} is not enough
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Non-Dinosaur spells are not reduced =====

    @Test
    @DisplayName("Non-Dinosaur creature spells are not reduced")
    void nonDinosaurSpellsNotReduced() {
        harness.addToBattlefield(player1, new KinjallisCaller());
        // Grizzly Bears costs {1}{G} — should not be reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only {G} is not enough for {1}{G}
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Stacking =====

    @Test
    @DisplayName("Two Kinjalli's Callers reduce Dinosaur spell cost by {2}")
    void twoCallersStackReduction() {
        harness.addToBattlefield(player1, new KinjallisCaller());
        harness.addToBattlefield(player1, new KinjallisCaller());
        // Frenzied Raptor costs {2}{R} — with {2} reduction it should cost just {R}
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Frenzied Raptor");
    }

    // ===== Opponent not affected =====

    @Test
    @DisplayName("Cost reduction does not apply to opponent's Dinosaur spells")
    void doesNotReduceOpponentDinosaurCosts() {
        harness.addToBattlefield(player1, new KinjallisCaller());
        // Opponent's Frenzied Raptor should still cost {2}{R}
        harness.setHand(player2, List.of(new FrenziedRaptor()));
        harness.addMana(player2, ManaColor.RED, 2);

        // Only {R}{R} is not enough for {2}{R} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
