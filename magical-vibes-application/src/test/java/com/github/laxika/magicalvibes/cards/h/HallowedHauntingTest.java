package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallowedHaunting.class, BadMoon.class, GrizzlyBears.class})
class HallowedHauntingTest extends BaseCardTest {

    @Test
    @DisplayName("Seven enchantments give your creatures flying and vigilance")
    void grantsFlyingAndVigilanceAtSevenEnchantments() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new BadMoon());
        }
        harness.addToBattlefield(player1, new HallowedHaunting());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Fewer than seven enchantments do not grant flying or vigilance")
    void doesNotGrantKeywordsBelowSevenEnchantments() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new BadMoon());
        }
        harness.addToBattlefield(player1, new HallowedHaunting());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Casting an enchantment creates a Spirit Cleric sized by controlled Spirits")
    void castingEnchantmentCreatesSizedSpiritCleric() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        harness.setHand(player1, List.of(new BadMoon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findSpiritCleric();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Spirit Cleric power and toughness update with the number of Spirits")
    void spiritClericUpdatesDynamically() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        harness.setHand(player1, List.of(new BadMoon(), new BadMoon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent firstToken = findSpiritCleric();
        assertThat(gqs.getEffectivePower(gd, firstToken)).isEqualTo(1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstToken)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Spirit Cleric".equals(p.getCard().getName()))
                .count()).isEqualTo(2);
    }

    private Permanent findSpiritCleric() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Spirit Cleric".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
