package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzoriusKnightArbiter.class})
class AzoriusKnightArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("Azorius Knight-Arbiter can't be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new AzoriusKnightArbiter());
        addCreatureReady(player1, new AzoriusKnightArbiter());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Vigilance keeps Azorius Knight-Arbiter untapped when it attacks")
    void vigilanceKeepsAttackerUntapped() {
        Permanent attacker = addCreatureReady(player1, new AzoriusKnightArbiter());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
    }
}
