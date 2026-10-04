package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Enchantmentize.class, SerraAngel.class, GloriousAnthem.class, Forest.class, Ornithopter.class})
class EnchantmentizeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature becomes only an enchantment")
    void enchantedCreatureBecomesOnlyAnEnchantment() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.setHand(player1, List.of(new Enchantmentize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, angel)).isTrue();
        assertThat(gqs.isCreature(gd, angel)).isFalse();
        assertThat(gqs.isLand(gd, angel)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can enchant an enchantment")
    void canEnchantAnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.setHand(player1, List.of(new Enchantmentize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(aura -> enchantment.getId().equals(aura.getAttachedTo()));
        assertThat(gqs.isEnchantment(gd, enchantment)).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura restores the enchanted creature")
    void removingAuraRestoresCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Enchantmentize());
        aura.setAttachedTo(angel.getId());

        assertThat(gqs.isCreature(gd, angel)).isFalse();
        assertThat(gqs.isEnchantment(gd, angel)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isCreature(gd, angel)).isTrue();
        assertThat(gqs.isEnchantment(gd, angel)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Enchantmentize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or enchantment");
    }

    @Test
    @DisplayName("An artifact creature loses both original card types and keeps its abilities")
    void artifactCreatureLosesBothOriginalTypes() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new Enchantmentize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, thopter.getId());
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, thopter)).isTrue();
        assertThat(gqs.isCreature(gd, thopter)).isFalse();
        assertThat(gqs.isArtifact(gd, thopter)).isFalse();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(aura -> thopter.getId().equals(aura.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted enchantment retains its static ability")
    void enchantedEnchantmentRetainsStaticAbility() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Enchantmentize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, anthem.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(5);
        assertThat(gqs.isEnchantment(gd, anthem)).isTrue();
    }

    @Test
    @DisplayName("Creature subtypes disappear while the creature is an enchantment")
    void creatureSubtypesDisappearUntilAuraLeaves() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Enchantmentize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.ANGEL)).isFalse();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(aura -> angel.getId().equals(aura.getAttachedTo()));

        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.ANGEL)).isTrue();
    }
}
