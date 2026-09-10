package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerratedBiskelion.class, BenalishKnight.class, MindStone.class})
class SerratedBiskelionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on itself and target creature")
    void putsCountersOnSourceAndTarget() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BenalishKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(knight.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(biskelion.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, biskelion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, biskelion)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mindStone.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(biskelion.isTapped()).isFalse();
        assertThat(biskelion.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent biskelion = addCreatureReady(player1, new SerratedBiskelion());

        harness.activateAbility(player1, 0, null, biskelion.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serrated Biskelion");
        harness.assertNotOnBattlefield(player1, "Serrated Biskelion");
    }
}
