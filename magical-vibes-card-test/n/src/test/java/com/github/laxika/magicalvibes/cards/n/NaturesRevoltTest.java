package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesRevolt.class, Forest.class, Mountain.class, GrizzlyBears.class, GloriousAnthem.class, TreetopVillage.class})
class NaturesRevoltTest extends BaseCardTest {

    @Test
    @DisplayName("Lands of both players become 2/2 creatures that are still lands")
    void animatesAllLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addToBattlefield(player1, new NaturesRevolt());

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        assertThat(gqs.isLand(gd, forest)).isTrue();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
        assertThat(gqs.isLand(gd, mountain)).isTrue();
    }

    @Test
    @DisplayName("Lands entering after Nature's Revolt also become 2/2 creatures")
    void animatesLandsThatEnterLater() {
        harness.addToBattlefield(player1, new NaturesRevolt());
        Permanent mountain = harness.enterBattlefieldAndReturn(player2, new Mountain());

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
        assertThat(gqs.isLand(gd, mountain)).isTrue();
    }

    @Test
    @DisplayName("Does not animate non-land permanents or change existing creatures")
    void doesNotAnimateNonLands() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NaturesRevolt());

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        // Grizzly Bears is a natural 2/2 — Nature's Revolt does not touch its P/T.
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated lands benefit from a creature anthem")
    void animatedLandsBenefitFromAnthem() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new NaturesRevolt());
        harness.addToBattlefield(player1, new GloriousAnthem());

        // 2/2 from Nature's Revolt + 1/1 from Glorious Anthem = 3/3.
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
    }

    @Test
    @DisplayName("A land entering under Nature's Revolt has summoning sickness")
    void enteringLandHasSummoningSickness() {
        harness.addToBattlefield(player1, new NaturesRevolt());
        Permanent mountain = harness.enterBattlefieldAndReturn(player1, new Mountain());

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(als.canAttack(gd, mountain, player1.getId())).isFalse();

        mountain.setSummoningSick(false);
        assertThat(als.canAttack(gd, mountain, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Lands revert to non-creatures when Nature's Revolt leaves")
    void revertsWhenLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent revolt = harness.addToBattlefieldAndReturn(player1, new NaturesRevolt());

        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, revolt));

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Resolving Nature's Revolt animates lands without animating the enchantment")
    void resolvingSpellAnimatesLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.castFromHand(player1, new NaturesRevolt(), "{3}{G}{G}");
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
        Permanent revolt = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof NaturesRevolt)
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, revolt)).isFalse();
    }

    @Test
    @DisplayName("Animated lands retain their mana ability and obey summoning sickness")
    void animatedLandManaAbilityRequiresNoSummoningSickness() {
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new NaturesRevolt());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        forest.setSummoningSick(false);
        harness.tapPermanent(player1, 0);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Counters and controller-specific anthems modify animated lands")
    void countersAndAnthemApplyAfterBasePowerAndToughness() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        forest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new NaturesRevolt());

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, mountain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isEqualTo(2);
    }

    @Test
    @CardUsed({NaturesRevolt.class, TreetopVillage.class})
    @DisplayName("A later Nature's Revolt overrides an earlier land animation's base power and toughness")
    void laterRevoltOverridesEarlierLandAnimation() {
        Permanent village = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, village)).isEqualTo(3);

        harness.castFromHand(player1, new NaturesRevolt(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, village)).isEqualTo(2);
        assertThat(gqs.isLand(gd, village)).isTrue();
        assertThat(gqs.isCreature(gd, village)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, village)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, village)).contains(CardSubtype.APE);
        assertThat(gqs.hasKeyword(gd, village, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @CardUsed({NaturesRevolt.class, TreetopVillage.class})
    @DisplayName("A later land animation overrides an earlier Nature's Revolt's base power and toughness")
    void laterLandAnimationOverridesEarlierRevolt() {
        Permanent village = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new NaturesRevolt());
        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, village)).isEqualTo(2);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, village)).isEqualTo(3);
        assertThat(gqs.isLand(gd, village)).isTrue();
        assertThat(gqs.hasKeyword(gd, village, Keyword.TRAMPLE)).isTrue();
    }
}
