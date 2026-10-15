package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianLions.class})
class GuardianLionsTest extends BaseCardTest {

    @Test
    void attackingDoesNotTapGuardianLions() {
        Permanent lions = addCreatureReady(player1, new GuardianLions());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(lions.isAttacking()).isTrue();
        assertThat(lions.isTapped()).isFalse();
    }

    @Test
    void vigilanceDoesNotAllowAttackingWhileTapped() {
        Permanent lions = addCreatureReady(player1, new GuardianLions());
        lions.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lions.isAttacking()).isFalse();
        assertThat(lions.isTapped()).isTrue();
    }

    @Test
    void vigilanceDoesNotBypassSummoningSickness() {
        Permanent lions = addCreatureReady(player1, new GuardianLions());
        lions.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lions.isAttacking()).isFalse();
        assertThat(lions.isTapped()).isFalse();
    }
}
