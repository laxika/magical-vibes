package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.cards.s.ShootTheSheriff;
import com.github.laxika.magicalvibes.cards.t.TakeUpTheShield;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrairieDog.class, TrainedArynx.class,
        ShootTheSheriff.class, TakeUpTheShield.class})
class PrairieDogTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself at the end step when no spell was cast from hand")
    void putsCounterWhenNoSpellWasCastFromHand() {
        Permanent dog = addPrairieDog();

        advanceToEndStep(player1);

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself after a hand spell was cast")
    void doesNotPutCounterAfterHandSpell() {
        Permanent dog = addPrairieDog();
        harness.setHand(player1, List.of(new TrainedArynx()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Adds one extra +1/+1 counter to a controlled creature until end of turn")
    void addsAnExtraCounterUntilEndOfTurn() {
        addPrairieDog();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        activateCounterReplacement();

        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, recipient.getId());

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counter replacement wears off at end of turn")
    void counterReplacementWearsOffAtEndOfTurn() {
        addPrairieDog();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        activateCounterReplacement();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, recipient.getId());

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(PrairieDog.class)
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent dog = addPrairieDog();
        advanceToEndStep(player2);
        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({PrairieDog.class, TakeUpTheShield.class})
    void rechecksHandCastingWhenEndStepTriggerResolves() {
        Permanent dog = addPrairieDog();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, dog.getId());
        harness.passBothPriorities();

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(PrairieDog.class)
    void multipleActivationsEachAddOneCounterToEndStepPlacement() {
        Permanent dog = addPrairieDog();
        activateCounterReplacement();
        activateCounterReplacement();
        advanceToEndStep(player1);
        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @CardUsed({PrairieDog.class, TakeUpTheShield.class, ShootTheSheriff.class})
    void replacementPersistsAfterSourceLeavesBattlefield() {
        Permanent source = addPrairieDog();
        Permanent recipient = addPrairieDog();
        activateCounterReplacement();
        destroyDog(source);

        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, recipient.getId());

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({PrairieDog.class, TakeUpTheShield.class, ShootTheSheriff.class})
    void replacementResolvesEvenIfSourceWasRemovedInResponse() {
        Permanent source = addPrairieDog();
        Permanent recipient = addPrairieDog();
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);
        destroyDog(source);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, recipient.getId());

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({PrairieDog.class, TakeUpTheShield.class})
    void doesNotIncreaseCountersPlacedByOpponentOnYourCreature() {
        Permanent dog = addPrairieDog();
        activateCounterReplacement();
        harness.setHand(player2, List.of(new TakeUpTheShield()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, dog.getId());

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void destroyDog(Permanent dog) {
        harness.setHand(player2, List.of(new ShootTheSheriff()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, dog.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dog);
    }

    private Permanent addPrairieDog() {
        return harness.addToBattlefieldAndReturn(player1, new PrairieDog());
    }

    private void activateCounterReplacement() {
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
