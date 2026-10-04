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

@CardUsed({HeroOfTheWinds.class, GiantGrowth.class, GrizzlyBears.class})
class HeroOfTheWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures when you cast a spell targeting Hero of the Winds")
    void boostsYourCreaturesWhenTargetedByOwnSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(player1, hero);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when your spell targets another creature")
    void doesNotTriggerWhenAnotherCreatureIsTargeted() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(player1, bears);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a spell targeting Hero of the Winds")
    void doesNotTriggerOnOpponentsSpell() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(player2, hero);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void boostResolvesBeforeTheTargetingSpellAndDoesNotAffectOpponents() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, hero.getId());

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(5);
    }

    @Test
    void repeatedTargetingSpellsGiveCumulativeBoosts() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiantGrowth(player1, hero);
        castGiantGrowth(player1, hero);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void boostDoesNotAffectCreaturesEnteringLaterAndExpiresAtEndOfTurn() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheWinds());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGiantGrowth(player1, hero);

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private void castGiantGrowth(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player);
        harness.castInstant(player, 0, target.getId());
        resolveAllTriggers();
    }
}
