package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OgreResister;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverskinArmor.class, OgreResister.class})
class SilverskinArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());

        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());

        armor.setAttachedTo(ogre.getId());

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature becomes an artifact in addition to its other types")
    void equippedCreatureBecomesArtifact() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());

        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());

        // Before equipping, creature is not an artifact
        assertThat(gqs.isArtifact(gd, ogre)).isFalse();

        armor.setAttachedTo(ogre.getId());

        // After equipping, creature is an artifact via static bonus
        assertThat(gqs.isArtifact(gd, ogre)).isTrue();

        // Verify via static bonus
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, ogre);
        assertThat(bonus.grantedCardTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Equipped creature retains original type while also being an artifact")
    void equippedCreatureRetainsOriginalType() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());

        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());

        armor.setAttachedTo(ogre.getId());

        // Original type is still Creature
        assertThat(gqs.isCreature(gd, ogre)).isTrue();
        // Also an artifact via static
        assertThat(gqs.isArtifact(gd, ogre)).isTrue();
    }

    @Test
    @DisplayName("Static effects removed when equipment is unequipped")
    void staticEffectsRemovedWhenUnequipped() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());

        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());

        // Attach
        armor.setAttachedTo(ogre.getId());
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(5);
        assertThat(gqs.isArtifact(gd, ogre)).isTrue();

        // Detach
        armor.setAttachedTo(null);

        // Boost gone
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(3);

        // No longer an artifact
        assertThat(gqs.isArtifact(gd, ogre)).isFalse();
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, ogre);
        assertThat(bonus.grantedCardTypes()).doesNotContain(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Equipped creature counts toward metalcraft")
    void equippedCreatureCountsTowardMetalcraft() {
        // Place two Equipment artifacts on the battlefield
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());

        harness.addToBattlefield(player1, new SilverskinArmor());

        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());

        // Before equipping: 2 artifacts (two Silverskin Armors), no metalcraft
        assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isFalse();

        // Equip ogre, making it the third artifact
        armor.setAttachedTo(ogre.getId());
        assertThat(gqs.isMetalcraftMet(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Equip requires two mana and grants both bonuses only on resolution")
    void equipRequiresTwoManaAndResolves() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ogre.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(armor.getAttachedTo()).isNull();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, ogre.getId());
        assertThat(armor.getAttachedTo()).isNull();
        assertThat(gqs.isArtifact(gd, ogre)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(ogre.getId());
        assertThat(armor.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(4);
        assertThat(gqs.isArtifact(gd, ogre)).isTrue();
        assertThat(gqs.isCreature(gd, ogre)).isTrue();
    }

    @Test
    @DisplayName("Reequipping transfers both bonuses from the old creature to the new creature")
    void reequippingTransfersBothBonuses() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new OgreResister());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new OgreResister());
        armor.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(gqs.isArtifact(gd, first)).isTrue();
        assertThat(gqs.isArtifact(gd, second)).isFalse();
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.isArtifact(gd, first)).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, second)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentCreature() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new OgreResister());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ogre.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(armor.getAttachedTo()).isNull();
        assertThat(gqs.isArtifact(gd, ogre)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipRequiresMainPhase() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ogre.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(armor.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated while an ability is on the stack")
    void equipRequiresEmptyStack() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor());
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, ogre.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ogre.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(armor.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(armor.getAttachedTo()).isEqualTo(ogre.getId());
    }
}
