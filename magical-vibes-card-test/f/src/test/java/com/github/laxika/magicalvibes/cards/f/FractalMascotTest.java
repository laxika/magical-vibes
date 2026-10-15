package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FractalMascot.class})
class FractalMascotTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target opposing creature and puts a stun counter on it")
    void etbTapsAndStunsTargetOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FractalMascot());
        castFractalMascot(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        UUID ownCreatureId = harness.addToBattlefieldAndReturn(player1, new FractalMascot()).getId();
        harness.setHand(player1, List.of(new FractalMascot()));
        addManaForFractalMascot();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alreadyTappedCreatureStillGetsStunCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FractalMascot());
        target.tap();
        castFractalMascot(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void stunReplacesOnlyTheNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FractalMascot());
        castFractalMascot(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void triggerStillResolvesAfterSourceLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FractalMascot());
        castFractalMascot(target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    private void castFractalMascot(UUID targetId) {
        harness.setHand(player1, List.of(new FractalMascot()));
        addManaForFractalMascot();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void addManaForFractalMascot() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
