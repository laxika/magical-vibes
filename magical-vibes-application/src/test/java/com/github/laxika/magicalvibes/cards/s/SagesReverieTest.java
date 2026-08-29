package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SagesReverieTest extends BaseCardTest {

    @Test
    @DisplayName("Draws for each Aura attached to a creature, including itself")
    void drawsForEachAuraAttachedToCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent existingAura = new Permanent(new UnholyStrength());
        existingAura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(existingAura);

        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(forest);
        Permanent auraAttachedToNoncreature = new Permanent(new AbundantGrowth());
        auraAttachedToNoncreature.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraAttachedToNoncreature);

        harness.setHand(player1, List.of(new SagesReverie()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Boosts the enchanted creature based on the controller's attached Auras")
    void boostsBasedOnControlledAurasAttachedToCreatures() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent reverie = new Permanent(new SagesReverie());
        reverie.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(reverie);

        Permanent existingAura = new Permanent(new UnholyStrength());
        existingAura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(existingAura);

        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(forest);
        Permanent auraAttachedToNoncreature = new Permanent(new AbundantGrowth());
        auraAttachedToNoncreature.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraAttachedToNoncreature);

        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);
        Permanent opponentAura = new Permanent(new UnholyStrength());
        opponentAura.setAttachedTo(opponentBears.getId());
        gd.playerBattlefields.get(player2.getId()).add(opponentAura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(forest);
        harness.setHand(player1, List.of(new SagesReverie()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
