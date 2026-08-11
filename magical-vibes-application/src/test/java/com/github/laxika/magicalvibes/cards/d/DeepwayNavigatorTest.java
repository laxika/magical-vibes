package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeepwayNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering untaps each other Merfolk but not other creature types")
    void enteringUntapsOtherMerfolk() {
        Permanent merfolk = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        merfolk.tap();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();

        harness.setHand(player1, List.of(new DeepwayNavigator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isFalse();
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Merfolk get +1/+0 after attacking with three Merfolk")
    void boostsMerfolkAfterThreeMerfolkAttack() {
        Permanent navigator = addCreatureReady(player1, new DeepwayNavigator());
        Permanent merfolk1 = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        Permanent merfolk2 = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThat(gqs.getEffectivePower(gd, navigator)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, merfolk1)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, merfolk2)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with fewer than three Merfolk does not grant the boost")
    void doesNotBoostAfterFewerThanThreeMerfolkAttack() {
        Permanent navigator = addCreatureReady(player1, new DeepwayNavigator());
        Permanent merfolk = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThat(gqs.getEffectivePower(gd, navigator)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }
}
