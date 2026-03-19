package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CabalStronghold;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.IncreaseEachPlayerCastCostPerSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandExcessManaWithColorlessEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DampingSphereTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Damping Sphere has correct static effects")
    void hasCorrectProperties() {
        DampingSphere card = new DampingSphere();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isInstanceOf(ReplaceLandExcessManaWithColorlessEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isInstanceOf(IncreaseEachPlayerCastCostPerSpellThisTurnEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Damping Sphere resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new DampingSphere()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Damping Sphere"));
    }

    // ===== Spell tax: costs {1} more per previously cast spell this turn =====

    @Test
    @DisplayName("First spell of the turn is not taxed")
    void firstSpellNotTaxed() {
        harness.addToBattlefield(player1, new DampingSphere());

        // Player1 casts a creature as the first spell — should cost normal {1}{G}
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Second spell of the turn costs {1} more")
    void secondSpellCostsOneMore() {
        harness.addToBattlefield(player1, new DampingSphere());

        // Cast first spell (costs normal)
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Second spell should cost {1}{G} + {1} tax = 3 total
        // We have 3 green mana remaining (5 - 2 for first spell)
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Second spell fails without enough mana for tax")
    void secondSpellFailsWithoutTaxMana() {
        harness.addToBattlefield(player1, new DampingSphere());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        // Cast first spell (costs 2 green)
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Only 2 green left, but second spell needs 3 (2 + 1 tax)
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Third spell costs {2} more")
    void thirdSpellCostsTwoMore() {
        harness.addToBattlefield(player1, new DampingSphere());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        // First spell: 2 mana (remaining: 7)
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Second spell: 2 + 1 tax = 3 mana (remaining: 4)
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Third spell: 2 + 2 tax = 4 mana (remaining: 0)
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Spell tax affects opponent too")
    void spellTaxAffectsOpponent() {
        harness.addToBattlefield(player1, new DampingSphere());

        // Player2 casts spells — tax should apply
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        // First spell: 2 mana (remaining: 2)
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        // Second spell needs 3, only have 2
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Two Damping Spheres double the tax")
    void twoDampingSpheresDoubleTheTax() {
        harness.addToBattlefield(player1, new DampingSphere());
        harness.addToBattlefield(player1, new DampingSphere());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        // First spell: 2 mana (remaining: 4)
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Second spell: 2 + 2 tax (1 per sphere × 1 spell cast) = 4 mana (remaining: 0)
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Spell tax stops after Damping Sphere leaves the battlefield")
    void spellTaxStopsAfterRemoval() {
        harness.addToBattlefield(player1, new DampingSphere());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        // First spell: 2 mana (remaining: 2)
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Remove Damping Sphere
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Damping Sphere"));

        // Second spell costs normal 2 (no tax)
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Mana replacement: lands tapped for 2+ mana produce {C} instead =====

    @Test
    @DisplayName("Basic land tapping for 1 mana is not affected")
    void basicLandNotAffected() {
        harness.addToBattlefield(player1, new DampingSphere());
        harness.addToBattlefield(player1, new Forest());

        // Tap Forest — produces 1 green, should not be replaced
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Land tapped for 2+ mana produces {C} instead")
    void multiManaLandProducesColorlessInstead() {
        harness.addToBattlefield(player1, new DampingSphere());
        harness.addToBattlefield(player1, new CabalStronghold());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        // Add 3 mana for activation cost of Cabal Stronghold's second ability ({3}, {T})
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Activate ability index 1 (the multi-mana ability) on CabalStronghold (index 1 on battlefield)
        // With 2 basic Swamps, normally produces 2B. With Damping Sphere, produces 1C instead.
        harness.activateAbility(player1, 1, 1, null, null);

        // After paying {3} and getting replacement: pool should have 1 colorless, 0 black
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    @DisplayName("Multi-mana land works normally without Damping Sphere")
    void multiManaLandWorksNormallyWithoutDampingSphere() {
        harness.addToBattlefield(player1, new CabalStronghold());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Activate Cabal Stronghold's second ability (index 1) — should produce 2B normally
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-land mana source producing 2+ mana is not affected")
    void nonLandManaSourceNotAffected() {
        harness.addToBattlefield(player1, new DampingSphere());

        // PalladiumMyr is a creature (not a land) that taps for 2 colorless
        Permanent myr = new Permanent(new com.github.laxika.magicalvibes.cards.p.PalladiumMyr());
        myr.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(myr);

        // Tap PalladiumMyr — produces 2 colorless, NOT replaced (not a land)
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
