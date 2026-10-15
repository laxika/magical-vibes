package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempestTechnique.class, BloodMoon.class, GrizzlyBears.class})
class TempestTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each enchantment its controller controls")
    void boostsByControlledEnchantmentCount() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BloodMoon());

        castTechnique(player1, creature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.addToBattlefield(player1, new BloodMoon());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Storm creates token copies for each spell cast before Tempest Technique")
    void stormCreatesTokenCopies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        castTechnique(player1, creature);

        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> techniques = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Tempest Technique"))
                .toList();
        assertThat(techniques).hasSize(2);
        assertThat(techniques).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TempestTechnique()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Only your enchantments count, including Tempest Technique itself")
    void excludesOpponentsEnchantmentsAndUnenchantedCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BloodMoon());

        castTechnique(player1, creature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Storm counts opponents' spells and copies may enchant different creatures")
    void stormCountsOpponentSpellAndRetargetsCopy() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.ensurePriority(player1);
        castTechnique(player1, originalTarget);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> techniques = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Tempest Technique"))
                .toList();
        assertThat(techniques).hasSize(2);
        assertThat(techniques).filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getAttachedTo()).isEqualTo(copyTarget.getId()));
        assertThat(techniques).filteredOn(permanent -> !permanent.getCard().isToken())
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getAttachedTo()).isEqualTo(originalTarget.getId()));
        assertThat(gqs.getEffectivePower(gd, originalTarget)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, originalTarget)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, copyTarget)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, copyTarget)).isEqualTo(4);
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple storm Auras each count every enchantment, including token copies")
    void multipleCopiesStackTheirDynamicBoosts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        castTechnique(player1, creature);

        harness.passBothPriorities();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Aura fails to resolve if its target changes controller")
    void targetMustStillBeControlledOnResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTechnique(player1, creature);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tempest Technique");
        harness.assertInGraveyard(player1, "Tempest Technique");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private void castTechnique(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new TempestTechnique()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 3);
        harness.castEnchantment(caster, 0, creature.getId());
    }
}
