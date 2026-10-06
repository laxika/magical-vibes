package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.e.ExposeTheCulprit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaltRoadAmbushers.class, DogWalker.class, ExposeTheCulprit.class, Forest.class})
class SaltRoadAmbushersTest extends BaseCardTest {

    @Test
    void putsTwoCountersOnAnotherCreatureThatTurnsFaceUp() {
        Permanent ambushers = addCreatureReady(player1, new SaltRoadAmbushers());
        harness.setHand(player1, List.of(new DogWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dogWalker = findPermanent(player1, "Dog Walker");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dogWalker));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(dogWalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ambushers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenSaltRoadAmbushersTurnsFaceUp() {
        harness.setHand(player1, List.of(new SaltRoadAmbushers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ambushers = findPermanent(player1, "Salt Road Ambushers");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ambushers));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(ambushers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenAnotherPermanentTurnsFaceUpAsANoncreature() {
        Permanent ambushers = addCreatureReady(player1, new SaltRoadAmbushers());
        Permanent faceDownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        faceDownForest.setFaceDownAsCloaked();

        harness.setHand(player1, List.of(new ExposeTheCulprit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(faceDownForest.getId()));
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(faceDownForest.isFaceDown()).isFalse();
        assertThat(faceDownForest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ambushers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotGetMegamorphCounterWhenTurnedFaceUpByASpell() {
        Permanent ambushers = harness.addToBattlefieldAndReturn(player1, new SaltRoadAmbushers());
        ambushers.setFaceDownAsCloaked();
        harness.setHand(player1, List.of(new ExposeTheCulprit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(ambushers.getId()));
        harness.passBothPriorities();

        assertThat(ambushers.isFaceDown()).isFalse();
        assertThat(ambushers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForAnOpponentsCreature() {
        Permanent ambushers = addCreatureReady(player1, new SaltRoadAmbushers());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DogWalker());
        opponentCreature.setFaceDownAsDisguised();
        harness.setHand(player1, List.of(new ExposeTheCulprit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(opponentCreature.getId()));
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(opponentCreature.isFaceDown()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ambushers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachAmbushersAddsCountersToAnotherCreature() {
        Permanent first = addCreatureReady(player1, new SaltRoadAmbushers());
        Permanent second = addCreatureReady(player1, new SaltRoadAmbushers());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DogWalker());
        creature.setFaceDownAsDisguised();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creature));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
