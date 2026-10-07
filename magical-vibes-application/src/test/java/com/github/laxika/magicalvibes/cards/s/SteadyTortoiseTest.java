package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarriedDash;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteadyTortoise.class, HarriedDash.class, GrizzlyBears.class, Shock.class, Clone.class})
class SteadyTortoiseTest extends BaseCardTest {

    @Test
    void adventureCreatesHastyRabbit() {
        SteadyTortoise tortoise = new SteadyTortoise();
        harness.setHand(player1, List.of(tortoise));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent rabbit = findPermanent(player1, "Rabbit");
        assertThat(rabbit.getEffectivePower()).isEqualTo(1);
        assertThat(rabbit.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.HASTE)).isTrue();
    }

    @Test
    void attackTriggerPerpetuallyBoostsTortoiseOncePerCombat() {
        SteadyTortoise tortoise = new SteadyTortoise();
        Permanent tortoisePermanent = addCreatureReady(player1, tortoise);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(bear),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondBear)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tortoisePermanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tortoisePermanent)).isEqualTo(4);
    }

    @Test
    void exileAttackTriggerPerpetuallyBoostsTortoise() {
        SteadyTortoise tortoise = new SteadyTortoise();
        harness.setExile(player1, List.of(tortoise));
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        harness.passBothPriorities();

        assertThat(gd.perpetualCardPowerToughnessModifiers.get(tortoise.getId()).power()).isEqualTo(1);
        assertThat(gd.perpetualCardPowerToughnessModifiers.get(tortoise.getId()).toughness()).isEqualTo(1);
    }

    @Test
    void rabbitLosesHasteAfterTheTurn() {
        harness.setHand(player1, List.of(new SteadyTortoise()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent rabbit = findPermanent(player1, "Rabbit");

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.HASTE)).isFalse();
        assertThat(countPermanents(player1, "Rabbit")).isEqualTo(1);
    }

    @Test
    void adventureExileBoostPersistsWhenCreatureIsCast() {
        SteadyTortoise tortoise = new SteadyTortoise();
        harness.setHand(player1, List.of(tortoise));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castFromExile(player1, tortoise.getId());
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Steady Tortoise");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void opponentAttackDoesNotBoostBattlefieldOrExiledTortoise() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player1, new SteadyTortoise());
        SteadyTortoise exiled = new SteadyTortoise();
        harness.setExile(player1, List.of(exiled));
        addCreatureReady(player2, new SteadyTortoise());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, tortoise)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tortoise)).isEqualTo(3);
        assertThat(gd.perpetualCardPowerToughnessModifiers).doesNotContainKey(exiled.getId());
    }

    @Test
    void wardCountersSpellWhenOpponentCannotPay() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player1, new SteadyTortoise());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, tortoise.getId());

        assertThat(tortoise.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void payingTwoManaForWardLetsSpellResolve() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player1, new SteadyTortoise());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, tortoise.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(tortoise.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void copiedAttackAbilityBoostsCopyInsteadOfOriginal() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new SteadyTortoise());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        Permanent copy = findPermanent(player1, "Steady Tortoise");
        copy.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
    }
}
