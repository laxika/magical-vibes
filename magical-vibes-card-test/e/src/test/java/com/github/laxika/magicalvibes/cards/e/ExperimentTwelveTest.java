package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DogWalker;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExperimentTwelve.class, DogWalker.class, ZoeticCavern.class})
class ExperimentTwelveTest extends BaseCardTest {

    @Test
    void putsCountersEqualToTheTurnedUpCreaturesPowerOnIt() {
        harness.setHand(player1, List.of(new ExperimentTwelve()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent experiment = findPermanent(player1, "Experiment Twelve");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(experiment));
        resolveAllTriggers();

        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void putsCountersEqualToAnotherControlledCreaturesPowerOnThatCreature() {
        Permanent experiment = addCreatureReady(player1, new ExperimentTwelve());
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

        assertThat(dogWalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(experiment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenControlledLandIsTurnedFaceUp() {
        addCreatureReady(player1, new ExperimentTwelve());
        harness.setHand(player1, List.of(new ZoeticCavern()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent cavern = findPermanent(player1, "Zoetic Cavern");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cavern));

        assertThat(cavern.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(cavern.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachExperimentRecalculatesPowerWhenItsTriggerResolves() {
        Permanent first = addCreatureReady(player1, new ExperimentTwelve());
        harness.setHand(player1, List.of(new ExperimentTwelve()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent turned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(turned));
        resolveAllTriggers();

        assertThat(turned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void faceDownAndOpposingExperimentsDoNotWatchTurnedCreature() {
        Permanent hidden = addCreatureReady(player1, new ExperimentTwelve());
        hidden.setFaceDownAsDisguised();
        Permanent opposing = addCreatureReady(player2, new ExperimentTwelve());
        harness.setHand(player1, List.of(new ExperimentTwelve()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent turned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != hidden).findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(turned));
        resolveAllTriggers();

        assertThat(turned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(hidden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
