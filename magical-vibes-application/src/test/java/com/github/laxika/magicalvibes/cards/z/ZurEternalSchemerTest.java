package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Insight;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.w.WeaverOfHarmony;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZurEternalSchemer.class, WeaverOfHarmony.class, GrizzlyBears.class, Insight.class,
        Pacifism.class})
class ZurEternalSchemerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives enchantment creatures you control deathtouch, lifelink, and hexproof")
    void grantsKeywordsToEnchantmentCreaturesYouControl() {
        harness.addToBattlefield(player1, new ZurEternalSchemer());
        Permanent ownEnchantmentCreature = harness.addToBattlefieldAndReturn(player1, new WeaverOfHarmony());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentEnchantmentCreature = harness.addToBattlefieldAndReturn(player2, new WeaverOfHarmony());

        assertThat(gqs.hasKeyword(gd, ownEnchantmentCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownEnchantmentCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownEnchantmentCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentEnchantmentCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Permanently animates a targeted non-Aura enchantment with its mana value as base power and toughness")
    void permanentlyAnimatesTargetedEnchantment() {
        Permanent zur = harness.addToBattlefieldAndReturn(player1, new ZurEternalSchemer());
        Permanent insight = harness.addToBattlefieldAndReturn(player1, new Insight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zur);
        harness.activateAbility(player1, sourceIndex, null, insight.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, insight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, insight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, insight)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(zur);
        assertThat(gqs.isCreature(gd, insight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, insight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, insight)).isEqualTo(3);
    }

    @Test
    void cannotTargetAnOpponentEnchantment() {
        Permanent zur = harness.addToBattlefieldAndReturn(player1, new ZurEternalSchemer());
        Permanent insight = harness.addToBattlefieldAndReturn(player2, new Insight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zur);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, insight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAurasOrNonenchantments() {
        Permanent zur = harness.addToBattlefieldAndReturn(player1, new ZurEternalSchemer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent pacifism = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        pacifism.setAttachedTo(bears.getId());
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zur);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, pacifism.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAnimateAnExistingEnchantmentCreatureWithoutLosingItsAbilities() {
        Permanent zur = harness.addToBattlefieldAndReturn(player1, new ZurEternalSchemer());
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new WeaverOfHarmony());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(zur),
                null, weaver.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(weaver);
        assertThat(gqs.getEffectivePower(gd, weaver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, weaver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.HEXPROOF)).isTrue();

        Permanent insight = harness.addToBattlefieldAndReturn(player1, new Insight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(zur),
                null, insight.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(insight);
        assertThat(gqs.isEnchantment(gd, insight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, insight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, insight)).isEqualTo(4);
    }

    @Test
    void animationResolvesAfterZurLeavesWithoutGrantingItsKeywords() {
        Permanent zur = harness.addToBattlefieldAndReturn(player1, new ZurEternalSchemer());
        Permanent insight = harness.addToBattlefieldAndReturn(player1, new Insight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(zur),
                null, insight.getId());
        gd.playerBattlefields.get(player1.getId()).remove(zur);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(insight);
        assertThat(gqs.isCreature(gd, insight)).isTrue();
        assertThat(gqs.isEnchantment(gd, insight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, insight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, insight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, insight, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, insight, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, insight, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void animationSurvivesTurnCleanup() {
        Permanent zur = harness.addToBattlefieldAndReturn(player1, new ZurEternalSchemer());
        Permanent insight = harness.addToBattlefieldAndReturn(player1, new Insight());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(zur),
                null, insight.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(insight);
        assertThat(gqs.isCreature(gd, insight)).isTrue();
        assertThat(gqs.isEnchantment(gd, insight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, insight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, insight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, insight, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, insight, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, insight, Keyword.HEXPROOF)).isTrue();
    }
}
