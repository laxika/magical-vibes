package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrawlingInfestation;
import com.github.laxika.magicalvibes.cards.s.SteelcladSpirit;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallowedHaunting.class, CrawlingInfestation.class, TravelingMinister.class, SteelcladSpirit.class})
class HallowedHauntingTest extends BaseCardTest {

    @Test
    @DisplayName("Seven enchantments give your creatures flying and vigilance")
    void grantsFlyingAndVigilanceAtSevenEnchantments() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new CrawlingInfestation());
        }
        harness.addToBattlefield(player1, new HallowedHaunting());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Fewer than seven enchantments do not grant flying or vigilance")
    void doesNotGrantKeywordsBelowSevenEnchantments() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new CrawlingInfestation());
        }
        harness.addToBattlefield(player1, new HallowedHaunting());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Casting an enchantment creates a Spirit Cleric sized by controlled Spirits")
    void castingEnchantmentCreatesSizedSpiritCleric() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        harness.castFromHand(player1, new CrawlingInfestation(), "{2}{G}");
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
        harness.setHand(player1, List.of(new CrawlingInfestation(), new CrawlingInfestation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
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

    @Test
    void losesGrantedKeywordsWhenEnchantmentCountDrops() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new CrawlingInfestation());
        }
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void opponentEnchantmentsDoNotCountAndOpponentCreaturesDoNotGainKeywords() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new CrawlingInfestation());
        }
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();

        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new CrawlingInfestation());
        }

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void tokenResolvesBeforeEnchantmentAndSurvivesSourceLeaving() {
        Permanent haunting = harness.addToBattlefieldAndReturn(player1, new HallowedHaunting());
        harness.castFromHand(player1, new CrawlingInfestation(), "{2}{G}");
        assertThat(gd.stack).hasSize(2);

        gd.playerBattlefields.get(player1.getId()).remove(haunting);
        harness.passBothPriorities();

        Permanent token = findSpiritCleric();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Crawling Infestation");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Crawling Infestation");
    }

    @Test
    void tokenCountsOnlyControlledSpiritsAndShrinksWhenOneLeaves() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        Permanent ownSpirit = harness.addToBattlefieldAndReturn(player1, new SteelcladSpirit());
        harness.addToBattlefield(player2, new SteelcladSpirit());
        harness.addToBattlefield(player1, new TravelingMinister());
        harness.castFromHand(player1, new CrawlingInfestation(), "{2}{G}");
        harness.passBothPriorities();

        Permanent token = findSpiritCleric();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(ownSpirit);

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void castingHauntingDoesNotTriggerItself() {
        harness.castFromHand(player1, new HallowedHaunting(), "{2}{W}{W}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void castingNonEnchantmentDoesNotCreateToken() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        harness.castFromHand(player1, new TravelingMinister(), "{W}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void opponentsEnchantmentSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CrawlingInfestation(), "{2}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void enchantmentEnteringWithoutBeingCastDoesNotCreateToken() {
        harness.addToBattlefield(player1, new HallowedHaunting());
        harness.enterBattlefieldAndReturn(player1, new CrawlingInfestation());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    private Permanent findSpiritCleric() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Spirit Cleric".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
