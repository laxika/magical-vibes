package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BondsOfFaith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartlessSummoning;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DanithaCapashenParagonTest extends BaseCardTest {

    // ===== Card effects =====

    @Test
    @DisplayName("Danitha has the cost reduction effect for Aura and Equipment subtypes")
    void hasCorrectEffects() {
        DanithaCapashenParagon card = new DanithaCapashenParagon();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(ReduceOwnCastCostForSubtypeEffect.class);

        ReduceOwnCastCostForSubtypeEffect effect = (ReduceOwnCastCostForSubtypeEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(effect.affectedSubtypes()).containsExactlyInAnyOrder(CardSubtype.AURA, CardSubtype.EQUIPMENT);
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== Aura cost reduction =====

    @Test
    @DisplayName("Aura spells cost {1} less to cast with Danitha on the battlefield")
    void auraSpellsCostOneLess() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        // Bonds of Faith costs {1}{W} — with {1} reduction it should cost just {W}
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bonds of Faith");
    }

    @Test
    @DisplayName("Cannot cast Aura without enough mana even with Danitha's cost reduction")
    void cannotCastAuraWithoutEnoughMana() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        // Bonds of Faith costs {1}{W} — with {1} reduction needs {W}; no mana is not enough
        harness.setHand(player1, List.of(new BondsOfFaith()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Equipment cost reduction =====

    @Test
    @DisplayName("Equipment spells cost {1} less to cast with Danitha on the battlefield")
    void equipmentSpellsCostOneLess() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        // Sylvok Lifestaff costs {1} — with {1} reduction it should cost {0}
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sylvok Lifestaff");
    }

    // ===== Non-Aura/Equipment spells are not reduced =====

    @Test
    @DisplayName("Non-Aura enchantment spells are not reduced by Danitha")
    void nonAuraEnchantmentsNotReduced() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        // Heartless Summoning costs {1}{B} — should not be reduced since it's not an Aura
        harness.setHand(player1, List.of(new HeartlessSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Only {B} is not enough for {1}{B}
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature spells are not reduced by Danitha")
    void creatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        // Grizzly Bears costs {1}{G} — should not be reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only {G} is not enough for {1}{G}
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Stacking =====

    @Test
    @DisplayName("Two Danithas reduce Aura and Equipment cost by {2}")
    void twoDanithasStackReduction() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        // Bonds of Faith costs {1}{W} — with {2} reduction the {1} generic is fully reduced, cost is {W}
        // (generic cost cannot go below 0)
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bonds of Faith");
    }

    // ===== Opponent not affected =====

    @Test
    @DisplayName("Danitha does not reduce opponent's Aura spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new DanithaCapashenParagon());
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);
        // Opponent's Bonds of Faith should still cost {1}{W}
        harness.setHand(player2, List.of(new BondsOfFaith()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        // Only {W} is not enough for {1}{W} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
