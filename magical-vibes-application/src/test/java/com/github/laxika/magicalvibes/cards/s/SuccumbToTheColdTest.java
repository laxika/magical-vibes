package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuccumbToTheCold.class, Forest.class, GrizzlyBears.class})
class SuccumbToTheColdTest extends BaseCardTest {

    @Test
    @DisplayName("Taps one target opponent creature and puts a stun counter on it")
    void tapsAndStunsOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(target));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps and stuns two target opponent creatures")
    void tapsAndStunsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(first, second));

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires at least one target")
    void requiresAtLeastOneTarget() {
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the spell's controller")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<Permanent> targets) {
        prepareCast();
        harness.castInstant(player1, 0, targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new SuccumbToTheCold()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
