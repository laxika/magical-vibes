package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteadfastGuard.class})
class SteadfastGuardTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent guard = addCreatureReady(player1, new SteadfastGuard());

        declareAttackers(List.of(0));

        assertThat(guard.isTapped()).isFalse();
    }

    @Test
    void dealsCombatDamageWithoutTappingWhenSecondPlayerAttacks() {
        Permanent guard = addCreatureReady(player2, new SteadfastGuard());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(guard.isTapped()).isFalse();
    }
}
