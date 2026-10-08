package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarportSecurity.class})
class StarportSecurityTest extends BaseCardTest {

    @Test
    @DisplayName("Taps another target creature and itself")
    void tapsAnotherCreature() {
        Permanent security = addSecurity();
        Permanent target = addCreatureReady(player2, new StarportSecurity());
        addMana(3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(security.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Costs two less with a controlled creature carrying a +1/+1 counter")
    void costsLessWithCounter() {
        addSecurity();
        Permanent counterCreature = addCreatureReady(player1, new StarportSecurity());
        counterCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new StarportSecurity());
        addMana(1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not get the reduction without a controlled creature carrying a +1/+1 counter")
    void requiresControlledCounterCreature() {
        addSecurity();
        Permanent opponentCreature = addCreatureReady(player2, new StarportSecurity());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addMana(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        Permanent security = addSecurity();
        addMana(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, security.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    void counterOnSourceReducesCostAndCanTapFriendlyCreature() {
        Permanent security = addSecurity();
        security.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player1, new StarportSecurity());
        addMana(1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(security.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        security.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void reductionDoesNotRemoveWhiteManaRequirement() {
        Permanent security = addSecurity();
        security.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new StarportSecurity());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(security.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void multipleCountersDoNotReduceCostMoreThanTwo() {
        Permanent security = addSecurity();
        security.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent other = addCreatureReady(player1, new StarportSecurity());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new StarportSecurity());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(security.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedCreatureIsLegalTarget() {
        Permanent security = addSecurity();
        Permanent target = addCreatureReady(player2, new StarportSecurity());
        target.tap();
        addMana(3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(security.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void summoningSickSourceCannotActivateTapAbility() {
        Permanent security = harness.addToBattlefieldAndReturn(player1, new StarportSecurity());
        security.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new StarportSecurity());
        addMana(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void tappedSourceCannotActivateAgain() {
        Permanent security = addSecurity();
        security.tap();
        Permanent target = addCreatureReady(player2, new StarportSecurity());
        addMana(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addSecurity() {
        return addCreatureReady(player1, new StarportSecurity());
    }

    private void addMana(int colorless) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
