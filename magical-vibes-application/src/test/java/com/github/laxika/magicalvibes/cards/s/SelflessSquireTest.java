package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelflessSquire.class, Shock.class})
class SelflessSquireTest extends BaseCardTest {

    @Test
    void preventsDamageAndPutsCountersForEachPointPrevented() {
        Permanent squire = castSquire();

        castShock(player2, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void preventionExpiresAtEndOfTurn() {
        Permanent squire = castSquire();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castShock(player2, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castSquire() {
        harness.setHand(player1, List.of(new SelflessSquire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SelflessSquire)
                .findFirst()
                .orElseThrow();
    }

    private void castShock(Player caster, UUID targetPlayerId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
