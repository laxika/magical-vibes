package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarportSecurity.class, GrizzlyBears.class})
class StarportSecurityTest extends BaseCardTest {

    @Test
    @DisplayName("Taps another target creature and itself")
    void tapsAnotherCreature() {
        Permanent security = addSecurity();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent counterCreature = addCreatureReady(player1, new GrizzlyBears());
        counterCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not get the reduction without a controlled creature carrying a +1/+1 counter")
    void requiresControlledCounterCreature() {
        addSecurity();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

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

    private Permanent addSecurity() {
        return addCreatureReady(player1, new StarportSecurity());
    }

    private void addMana(int colorless) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
