package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WaryWatchdog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfImmobility.class, WaryWatchdog.class})
class FearOfImmobilityTest extends BaseCardTest {

    @Test
    void tapsAndStunsOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WaryWatchdog());

        castFearOfImmobility(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void tapsOwnCreatureWithoutPuttingOnStunCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WaryWatchdog());

        castFearOfImmobility(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void mayChooseNoTarget() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WaryWatchdog());
        harness.setHand(player1, List.of(new FearOfImmobility()));
        addManaForFearOfImmobility();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void putsStunCounterOnAlreadyTappedOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WaryWatchdog());
        target.tap();

        castFearOfImmobility(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void repeatedTriggersAccumulateStunCountersAndPreventSuccessiveUntaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WaryWatchdog());

        castFearOfImmobility(target);
        castFearOfImmobility(target);

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetItselfOnEnteringAnOtherwiseEmptyBattlefield() {
        harness.setHand(player1, List.of(new FearOfImmobility()));
        addManaForFearOfImmobility();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent fear = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, fear.getId());
        resolveAllTriggers();

        assertThat(fear.isTapped()).isTrue();
        assertThat(fear.getCounterCount(CounterType.STUN)).isZero();
    }

    private void castFearOfImmobility(Permanent target) {
        harness.setHand(player1, List.of(new FearOfImmobility()));
        addManaForFearOfImmobility();
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();
    }

    private void addManaForFearOfImmobility() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
