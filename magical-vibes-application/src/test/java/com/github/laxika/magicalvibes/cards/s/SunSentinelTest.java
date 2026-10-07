package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunSentinel.class})
class SunSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Sun Sentinel untapped after attacking")
    void vigilanceDoesNotTapOnAttack() {
        Permanent sentinel = addCreatureReady(player1, new SunSentinel());

        declareAttackers(List.of(0));

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Sun Sentinel to attack")
    void tappedSentinelCannotAttack() {
        Permanent sentinel = addCreatureReady(player1, new SunSentinel());
        sentinel.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Vigilance does not allow Sun Sentinel to attack with summoning sickness")
    void summoningSickSentinelCannotAttack() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SunSentinel());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sentinel.isAttacking()).isFalse();
        assertThat(sentinel.isTapped()).isFalse();
    }
}
