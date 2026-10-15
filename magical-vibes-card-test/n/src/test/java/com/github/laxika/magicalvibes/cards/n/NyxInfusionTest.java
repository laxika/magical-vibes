package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.ArmoryOfIroas;
import com.github.laxika.magicalvibes.cards.g.GrimGuardian;
import com.github.laxika.magicalvibes.cards.r.RottedHulk;
import com.github.laxika.magicalvibes.cards.s.SatyrGrovedancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NyxInfusion.class, GrimGuardian.class, RottedHulk.class, ArmoryOfIroas.class, SatyrGrovedancer.class})
class NyxInfusionTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NyxInfusion());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchantment creature enchanted with Nyx Infusion gets +2/+2")
    void enchantmentCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrimGuardian());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        attach(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Non-enchantment creature enchanted with Nyx Infusion gets -2/-2")
    void nonEnchantmentCreatureGetsPenalty() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        attach(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower - 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness - 2);
    }

    @Test
    @DisplayName("Nyx Infusion's modification wears off when it leaves the battlefield")
    void effectStopsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        Permanent aura = attach(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower - 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness - 2);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Resolving Nyx Infusion attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RottedHulk());

        harness.setHand(player1, List.of(new NyxInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nyx Infusion")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Nyx Infusion cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ArmoryOfIroas());
        harness.setHand(player1, List.of(new NyxInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Nyx Infusion kills a small non-enchantment creature and goes to the graveyard")
    void lethalPenaltyAlsoRemovesAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrGrovedancer());
        harness.setHand(player1, List.of(new NyxInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Satyr Grovedancer");
        harness.assertInGraveyard(player2, "Satyr Grovedancer");
        harness.assertNotOnBattlefield(player1, "Nyx Infusion");
        harness.assertInGraveyard(player1, "Nyx Infusion");
    }

    @Test
    @DisplayName("Nyx Infusion does not enter when its target leaves before resolution")
    void missingTargetPreventsAuraEntering() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RottedHulk());
        harness.setHand(player1, List.of(new NyxInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyx Infusion");
        harness.assertInGraveyard(player1, "Nyx Infusion");
        assertThat(gd.stack).isEmpty();
    }
}
