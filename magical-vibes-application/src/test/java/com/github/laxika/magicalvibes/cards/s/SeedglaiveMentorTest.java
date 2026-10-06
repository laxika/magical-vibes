package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMeek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedglaiveMentor.class, GiantGrowth.class, ShortBow.class, MightOfTheMeek.class})
class SeedglaiveMentorTest extends BaseCardTest {

    @Test
    void valiantPutsOnlyOneCounterOnItselfWhenTargetedByYourSpellsEachTurn() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SeedglaiveMentor());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, mentor.getId());
        resolveAllTriggers();

        harness.castInstant(player1, 0, mentor.getId());
        resolveAllTriggers();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SeedglaiveMentor());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, mentor.getId());
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({ShortBow.class, MightOfTheMeek.class})
    void abilityAndSpellShareTheSameValiantLimit() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SeedglaiveMentor());
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new ShortBow());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new SeedglaiveMentor()));

        harness.activateAbility(player1, 1, null, mentor.getId());
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bow.getAttachedTo()).isNull();
        resolveAllTriggers();
        assertThat(bow.getAttachedTo()).isEqualTo(mentor.getId());

        harness.castInstant(player1, 0, mentor.getId());
        resolveAllTriggers();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(MightOfTheMeek.class)
    void opponentsTargetingDoesNotConsumeYourFirstTargetingEvent() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SeedglaiveMentor());
        harness.setHand(player1, List.of(new MightOfTheMeek()));
        harness.setHand(player2, List.of(new MightOfTheMeek()));
        harness.setLibrary(player1, List.of(new SeedglaiveMentor()));
        harness.setLibrary(player2, List.of(new SeedglaiveMentor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, mentor.getId());
        resolveAllTriggers();
        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castInstant(player1, 0, mentor.getId());
        resolveAllTriggers();
        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(ShortBow.class)
    void valiantIsAvailableAgainOnTheNextTurn() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SeedglaiveMentor());
        harness.addToBattlefield(player1, new ShortBow());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new SeedglaiveMentor()));
        harness.setLibrary(player2, List.of(new SeedglaiveMentor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, mentor.getId());
        resolveAllTriggers();
        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, mentor.getId());
        resolveAllTriggers();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed(ShortBow.class)
    void eachMentorHasItsOwnValiantLimit() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SeedglaiveMentor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SeedglaiveMentor());
        harness.addToBattlefield(player1, new ShortBow());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 2, null, first.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, 2, null, second.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
