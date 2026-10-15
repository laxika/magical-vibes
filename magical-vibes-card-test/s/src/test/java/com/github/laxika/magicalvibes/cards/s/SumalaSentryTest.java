package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.e.ExposeTheCulprit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SumalaSentry.class, DogWalker.class, ExposeTheCulprit.class, Forest.class, Murder.class})
class SumalaSentryTest extends BaseCardTest {

    @Test
    void turningFaceUpPutsCountersOnThePermanentAndSumalaSentry() {
        Permanent sentry = addCreatureReady(player1, new SumalaSentry());
        harness.setHand(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dogWalker = findPermanent(player1, "Dog Walker");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dogWalker));
        resolveAllTriggers();

        assertThat(dogWalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void turningUpAFaceDownNoncreaturePermanentAlsoTriggersSumalaSentry() {
        Permanent sentry = addCreatureReady(player1, new SumalaSentry());
        Permanent faceDownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        faceDownForest.setFaceDownAsCloaked();

        harness.setHand(player1, List.of(new ExposeTheCulprit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(faceDownForest.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(faceDownForest.isFaceDown()).isFalse();
        assertThat(faceDownForest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cloakedSentryGetsTwoCountersWhenItTurnsFaceUp() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new SumalaSentry());
        sentry.setFaceDownAsCloaked();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.turnFaceUp(player1, 0);
        resolveAllTriggers();

        assertThat(sentry.isFaceDown()).isFalse();
        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opposingPermanentTurningFaceUpDoesNotTriggerSentry() {
        Permanent sentry = addCreatureReady(player1, new SumalaSentry());
        Permanent dogWalker = harness.addToBattlefieldAndReturn(player2, new DogWalker());
        dogWalker.setFaceDownAsCloaked();
        harness.addMana(player2, ManaColor.RED, 2);

        harness.turnFaceUp(player2, 0);
        resolveAllTriggers();

        assertThat(dogWalker.isFaceDown()).isFalse();
        assertThat(dogWalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void sentryStillGetsCounterWhenTurnedPermanentDiesBeforeResolution() {
        Permanent sentry = addCreatureReady(player1, new SumalaSentry());
        Permanent dogWalker = harness.addToBattlefieldAndReturn(player1, new DogWalker());
        dogWalker.setFaceDownAsCloaked();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, 1);
        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, dogWalker.getId());
        harness.assertInGraveyard(player1, "Dog Walker");
        resolveAllTriggers();

        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void turnedPermanentStillGetsCounterWhenSentryDiesBeforeResolution() {
        Permanent sentry = addCreatureReady(player1, new SumalaSentry());
        Permanent dogWalker = harness.addToBattlefieldAndReturn(player1, new DogWalker());
        dogWalker.setFaceDownAsCloaked();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, 1);
        assertThat(dogWalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sentry.getId());
        harness.assertInGraveyard(player1, "Sumala Sentry");
        resolveAllTriggers();

        assertThat(dogWalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
