package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrowExtraArms.class, GiantSpider.class, GrizzlyBears.class, FountainOfYouth.class, Unsummon.class})
class GrowExtraArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {G} when targeting a Spider")
    void costsLessWhenTargetingSpider() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new GrowExtraArms()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        assertThat(spider.getPowerModifier()).isEqualTo(4);
        assertThat(spider.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Requires the full cost when targeting a non-Spider")
    void requiresFullCostWhenTargetingNonSpider() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrowExtraArms()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The +4/+4 boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrowExtraArms()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GrowExtraArms()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The discount also applies to an opponent's Spider")
    void discountsOpponentsSpider() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new GrowExtraArms()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        assertThat(spider.getPowerModifier()).isEqualTo(4);
        assertThat(spider.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("A Spider elsewhere on the battlefield does not discount a non-Spider target")
    void unrelatedSpiderDoesNotReduceCost() {
        harness.addToBattlefield(player1, new GiantSpider());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrowExtraArms()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The discount does not remove the green mana requirement")
    void discountDoesNotReduceColoredCost() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new GrowExtraArms()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spider.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolves at full cost on a non-Spider without boosting other creatures")
    void boostsOnlyNonSpiderTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.setHand(player1, List.of(new GrowExtraArms()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
        assertThat(spider.getPowerModifier()).isZero();
        assertThat(spider.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not resolve when its target leaves the battlefield in response")
    void targetLeavesBeforeResolution() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrowExtraArms(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, spider.getId());
        harness.castAndResolveInstant(player1, 0, spider.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInHand(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Grow Extra Arms");
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }
}
