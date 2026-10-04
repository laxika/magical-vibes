package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EncroachingMycosynth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeliodGodOfTheSun.class, GrizzlyBears.class, SuntailHawk.class,
        WrathOfGod.class, EncroachingMycosynth.class})
class HeliodGodOfTheSunTest extends BaseCardTest {

    @Test
    @DisplayName("Heliod is not a creature below five devotion to white")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent heliod = addHeliod();
        addWhitePermanents(3);

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("Heliod becomes a creature at five devotion to white")
    void becomesCreatureAtDevotionThreshold() {
        Permanent heliod = addHeliod();
        addWhitePermanents(4);

        assertThat(gqs.isCreature(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("Other creatures you control have vigilance")
    void grantsVigilanceToOtherCreatures() {
        Permanent heliod = addHeliod();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, heliod, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Heliod creates a 2/1 white Cleric enchantment creature token")
    void createsClericEnchantmentCreatureToken() {
        Permanent heliod = addHeliod();
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(heliod), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Cleric");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
    }

    @Test
    void losesCreatureTypeWhenDevotionDropsAndRegainsItWhenDevotionReturns() {
        Permanent heliod = addHeliod();
        addWhitePermanents(3);
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        assertThat(gqs.isCreature(gd, heliod)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, hawk));

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
        harness.addToBattlefield(player1, new SuntailHawk());
        assertThat(gqs.isCreature(gd, heliod)).isTrue();
    }

    @Test
    void opponentsWhitePermanentsDoNotCountTowardDevotion() {
        Permanent heliod = addHeliod();
        addWhitePermanents(3);
        harness.addToBattlefield(player2, new SuntailHawk());

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
    }

    @Test
    void vigilanceExcludesHeliodEvenWhenItIsACreatureAndExcludesOpponents() {
        Permanent heliod = addHeliod();
        addWhitePermanents(4);
        Permanent opponentHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gqs.isCreature(gd, heliod)).isTrue();
        assertThat(gqs.hasKeyword(gd, heliod, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHawk, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void survivesDestroyingAllCreaturesAndThenStopsBeingACreature() {
        Permanent heliod = addHeliod();
        addWhitePermanents(4);
        assertThat(gqs.isCreature(gd, heliod)).isTrue();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heliod, God of the Sun");
        assertThat(findPermanents(player1, "Suntail Hawk")).isEmpty();
        assertThat(gqs.isCreature(gd, heliod)).isFalse();
    }

    @Test
    void canCreateMultipleTokensWithoutTappingAndTokensDoNotIncreaseDevotion() {
        Permanent heliod = addHeliod();
        addWhitePermanents(3);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cleric")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CLERIC);
            assertThat(gqs.isCreature(gd, token)).isTrue();
            assertThat(gqs.isEnchantment(gd, token)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(heliod.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, heliod)).isFalse();
    }

    @Test
    void tokenAbilityResolvesAfterHeliodLeavesBattlefield() {
        Permanent heliod = addHeliod();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, heliod));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Cleric");
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
        assertThat(countPermanents(player1, "Cleric")).isEqualTo(1);
    }

    @Test
    void retainsArtifactTypeGrantedByEarlierMycosynthBelowDevotionThreshold() {
        harness.enterBattlefieldAndReturn(player1, new EncroachingMycosynth());
        Permanent heliod = harness.enterBattlefieldAndReturn(player1, new HeliodGodOfTheSun());

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
        assertThat(gqs.isArtifact(gd, heliod)).isTrue();
    }

    private Permanent addHeliod() {
        return harness.addToBattlefieldAndReturn(player1, new HeliodGodOfTheSun());
    }

    private void addWhitePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }
    }
}
