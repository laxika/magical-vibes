package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonoistSentry.class, GrizzlyBears.class})
class MonoistSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack because it has defender")
    void cannotAttackBecauseOfDefender() {
        Permanent sentry = addCreatureReady(player1, new MonoistSentry());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentry.isAttacking()).isFalse();
    }
}
