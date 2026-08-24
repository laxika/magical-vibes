package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakeUpTheShield.class, GrizzlyBears.class, Plains.class})
class TakeUpTheShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on the target and grants lifelink and indestructible")
    void putsCounterAndGrantsKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTakeUpTheShield(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Temporary keyword grants expire at end of turn but the counter remains")
    void keywordsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTakeUpTheShield(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castTakeUpTheShield(Permanent target) {
        harness.setHand(player1, List.of(new TakeUpTheShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
