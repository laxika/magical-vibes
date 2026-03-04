package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SilverskinArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Silverskin Armor has correct static effects")
    void hasCorrectStaticEffects() {
        SilverskinArmor card = new SilverskinArmor();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(BoostAttachedCreatureEffect.class);
        BoostAttachedCreatureEffect boost = (BoostAttachedCreatureEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);

        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(GrantCardTypeEffect.class);
        GrantCardTypeEffect grantType = (GrantCardTypeEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(grantType.cardType()).isEqualTo(CardType.ARTIFACT);
        assertThat(grantType.scope()).isEqualTo(GrantScope.EQUIPPED_CREATURE);
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent armor = new Permanent(new SilverskinArmor());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        armor.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Equipped creature becomes an artifact in addition to its other types")
    void equippedCreatureBecomesArtifact() {
        Permanent armor = new Permanent(new SilverskinArmor());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Before equipping, creature is not an artifact
        assertThat(gqs.isArtifact(gd, bears)).isFalse();

        armor.setAttachedTo(bears.getId());

        // After equipping, creature is an artifact via static bonus
        assertThat(gqs.isArtifact(gd, bears)).isTrue();

        // Verify via static bonus
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.grantedCardTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Equipped creature retains original type while also being an artifact")
    void equippedCreatureRetainsOriginalType() {
        Permanent armor = new Permanent(new SilverskinArmor());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        armor.setAttachedTo(bears.getId());

        // Original type is still Creature
        assertThat(bears.getCard().getType()).isEqualTo(CardType.CREATURE);
        // Also an artifact via static
        assertThat(gqs.isArtifact(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Static effects removed when equipment is unequipped")
    void staticEffectsRemovedWhenUnequipped() {
        Permanent armor = new Permanent(new SilverskinArmor());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Attach
        armor.setAttachedTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, bears)).isTrue();

        // Detach
        armor.setAttachedTo(null);

        // Boost gone
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        // No longer an artifact
        assertThat(gqs.isArtifact(gd, bears)).isFalse();
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bears);
        assertThat(bonus.grantedCardTypes()).doesNotContain(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Equipped creature counts toward metalcraft")
    void equippedCreatureCountsTowardMetalcraft() {
        // Place two artifacts + the armor on the battlefield
        Permanent armor = new Permanent(new SilverskinArmor());
        gd.playerBattlefields.get(player1.getId()).add(armor);

        Permanent artifact2 = new Permanent(new SilverskinArmor());
        gd.playerBattlefields.get(player1.getId()).add(artifact2);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Before equipping: 2 artifacts (two Silverskin Armors), no metalcraft
        assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isFalse();

        // Equip bears — now bears is also an artifact, total = 3
        armor.setAttachedTo(bears.getId());
        assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isTrue();
    }
}
