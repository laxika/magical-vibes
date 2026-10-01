package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GreatbowDoyen;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChangelingSentinel.class, GreatbowDoyen.class})
class ChangelingSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling gets the Archer boost from Greatbow Doyen")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new GreatbowDoyen());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new ChangelingSentinel());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Vigilance: Changeling Sentinel does not tap when declared as attacker")
    void vigilancePreventsTapWhenAttacking() {
        Permanent sentinel = addCreatureReady(player1, new ChangelingSentinel());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(sentinel.isTapped()).isFalse();
    }
}
