package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({AngelicReward.class, SanctuaryCat.class, Plains.class})
class AngelicRewardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Angelic Reward attaches it and grants +3/+3 and flying")
    void resolvingAttachesAndBoosts() {
        Permanent cat = addCreatureReady(player1, new SanctuaryCat());
        harness.setHand(player1, List.of(new AngelicReward()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, cat.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Angelic Reward");
        assertThat(aura.getAttachedTo()).isEqualTo(cat.getId());
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The creature loses Angelic Reward's effects when the Aura is removed")
    void effectsStopWhenRemoved() {
        Permanent cat = addCreatureReady(player1, new SanctuaryCat());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AngelicReward());
        aura.setAttachedTo(cat.getId());

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Angelic Reward fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent cat = addCreatureReady(player1, new SanctuaryCat());
        harness.setHand(player1, List.of(new AngelicReward()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, cat.getId());
        gd.playerBattlefields.get(player1.getId()).remove(cat);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angelic Reward");
        harness.assertNotOnBattlefield(player1, "Angelic Reward");
    }

    @Test
    @DisplayName("Angelic Reward cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new AngelicReward()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Angelic Reward can enchant an opponent's creature without affecting other creatures")
    void enchantsOpponentsCreatureOnly() {
        Permanent ownCat = addCreatureReady(player1, new SanctuaryCat());
        Permanent opposingCat = addCreatureReady(player2, new SanctuaryCat());
        harness.setHand(player1, List.of(new AngelicReward()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, opposingCat.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Angelic Reward").getAttachedTo()).isEqualTo(opposingCat.getId());
        assertThat(gqs.getEffectivePower(gd, opposingCat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCat)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, opposingCat, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCat, Keyword.FLYING)).isFalse();
        harness.assertOnBattlefield(player2, "Sanctuary Cat");
    }

    @Test
    @DisplayName("Multiple Angelic Rewards stack their bonuses and removing one preserves the other")
    void multipleRewardsStack() {
        Permanent cat = addCreatureReady(player1, new SanctuaryCat());
        harness.setHand(player1, List.of(new AngelicReward(), new AngelicReward()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castEnchantment(player1, 0, cat.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, cat.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Angelic Reward"));

        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Angelic Reward goes to its owner's graveyard when the enchanted creature leaves")
    void auraGoesToGraveyardWhenCreatureLeaves() {
        Permanent cat = addCreatureReady(player2, new SanctuaryCat());
        harness.setHand(player1, List.of(new AngelicReward()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, cat.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(cat);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Angelic Reward");
        harness.assertInGraveyard(player1, "Angelic Reward");
        harness.assertNotInGraveyard(player2, "Angelic Reward");
    }
}
