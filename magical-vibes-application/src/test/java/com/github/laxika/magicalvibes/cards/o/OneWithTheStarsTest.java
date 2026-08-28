package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OneWithTheStars.class, SerraAngel.class, GloriousAnthem.class, Forest.class})
class OneWithTheStarsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature becomes only an enchantment and keeps its abilities")
    void enchantedCreatureBecomesOnlyAnEnchantment() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.setHand(player1, List.of(new OneWithTheStars()));
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

        harness.setHand(player1, List.of(new OneWithTheStars()));
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
        Permanent aura = new Permanent(new OneWithTheStars());
        aura.setAttachedTo(angel.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

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
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or enchantment");
    }
}
