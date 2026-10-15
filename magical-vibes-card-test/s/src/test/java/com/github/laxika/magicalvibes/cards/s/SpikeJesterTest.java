package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeJester.class})
class SpikeJesterTest extends BaseCardTest {

    @Test
    void canAttackTheTurnItEnters() {
        harness.castFromHand(player1, new SpikeJester(), "{B}{R}");
        harness.passBothPriorities();
        Permanent jester = findPermanent(player1, "Spike Jester");

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(jester.isTapped()).isTrue();
        harness.assertLife(player2, 17);
    }

    @Test
    void hasteDoesNotAllowAttackingWhileTapped() {
        harness.castFromHand(player1, new SpikeJester(), "{B}{R}");
        harness.passBothPriorities();
        findPermanent(player1, "Spike Jester").tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        harness.assertLife(player2, 20);
    }
}
