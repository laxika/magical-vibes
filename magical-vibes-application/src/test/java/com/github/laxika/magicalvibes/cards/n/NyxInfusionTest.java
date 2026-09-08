package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HopefulEidolon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NyxInfusionTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent aura = new Permanent(new NyxInfusion());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchantment creature enchanted with Nyx Infusion gets +2/+2")
    void enchantmentCreatureGetsBoost() {
        Permanent creature = new Permanent(new HopefulEidolon());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        attach(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Non-enchantment creature enchanted with Nyx Infusion gets -2/-2")
    void nonEnchantmentCreatureGetsPenalty() {
        Permanent creature = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        attach(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower - 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness - 2);
    }

    @Test
    @DisplayName("Nyx Infusion's modification wears off when it leaves the battlefield")
    void effectStopsWhenAuraLeaves() {
        Permanent creature = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(creature);
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
        Permanent creature = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(creature);

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
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new NyxInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
