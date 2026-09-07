package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroOfTheNyxborn.class, GiantGrowth.class, GrizzlyBears.class})
class HeroOfTheNyxbornTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 1/1 white Human Soldier token")
    void entersWithHumanSoldierToken() {
        harness.enterBattlefieldAndReturn(player1, new HeroOfTheNyxborn());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boosts your creatures when you cast a spell targeting Hero of the Nyxborn")
    void boostsYourCreaturesWhenTargetedByOwnSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheNyxborn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(hero);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when your spell targets another creature")
    void doesNotTriggerWhenAnotherCreatureIsTargeted() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheNyxborn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(bears);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("The bonus wears off at end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheNyxborn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(hero);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private void castGiantGrowth(Permanent target) {
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
