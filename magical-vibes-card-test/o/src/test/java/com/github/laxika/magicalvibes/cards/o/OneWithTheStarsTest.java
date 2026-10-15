package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SpikeshotElder;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({OneWithTheStars.class, SerraAngel.class, GloriousAnthem.class, Forest.class, SpikeshotElder.class})
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
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OneWithTheStars());
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
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or enchantment");
    }

    @Test
    @DisplayName("Losing the creature type also removes creature subtypes")
    void removesCreatureSubtypes() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, angel)).isTrue();
        assertThat(gqs.isCreature(gd, angel)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).doesNotContain(CardSubtype.ANGEL);
    }

    @Test
    @DisplayName("Retained activated abilities use zero for the noncreature's power")
    void retainedAbilityUsesZeroPower() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new SpikeshotElder());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, elder.getId());
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, elder)).isTrue();
        assertThat(gqs.isCreature(gd, elder)).isFalse();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
