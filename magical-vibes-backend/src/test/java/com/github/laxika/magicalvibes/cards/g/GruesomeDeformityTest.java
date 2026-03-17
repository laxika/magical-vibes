package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GruesomeDeformityTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Gruesome Deformity has correct effects")
    void hasCorrectEffects() {
        GruesomeDeformity card = new GruesomeDeformity();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(effect.keyword()).isEqualTo(Keyword.INTIMIDATE);
        assertThat(effect.scope()).isEqualTo(GrantScope.ENCHANTED_CREATURE);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Gruesome Deformity")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new GruesomeDeformity()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Gruesome Deformity")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GruesomeDeformity()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Intimidate =====

    @Test
    @DisplayName("Enchanted creature has intimidate")
    void enchantedCreatureHasIntimidate() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new GruesomeDeformity());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    // ===== Effects stop when aura is removed =====

    @Test
    @DisplayName("Creature loses intimidate when Gruesome Deformity is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new GruesomeDeformity());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }
}
