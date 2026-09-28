package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Indicate.class, GrizzlyBears.class, Island.class})
class IndicateTest extends BaseCardTest {

    @Test
    void targetsAnyPermanentAndResolvesWithoutChangingIt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Indicate()));

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Indicate");
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new Indicate()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }
}
