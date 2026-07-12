package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncrementalBlightTest extends BaseCardTest {

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.BLACK, amount);
    }

    @Test
    @DisplayName("Places 1, 2 and 3 -1/-1 counters on the three targets respectively")
    void placesCountersOnEachTarget() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        List<Permanent> enemy = gd.playerBattlefields.get(player2.getId());
        UUID firstId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        UUID secondId = enemy.get(0).getId();
        UUID thirdId = enemy.get(1).getId();

        harness.castSorcery(player1, 0, List.of(firstId, secondId, thirdId));
        harness.passBothPriorities();

        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(enemy.get(0).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(enemy.get(1).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enough -1/-1 counters destroy a creature")
    void countersDestroyCreature() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        UUID firstId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        UUID angelId = harness.getPermanentId(player2, "Serra Angel");
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        // 2 counters on the Serra Angel (survives), 3 counters on the 2/2 Grizzly Bears (dies).
        harness.castSorcery(player1, 0, List.of(firstId, angelId, bearId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target the same creature more than once")
    void cannotTargetSameCreatureTwice() {
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        addMana(5);

        UUID firstId = gd.playerBattlefields.get(player1.getId()).getFirst().getId();
        UUID secondId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(firstId, secondId, firstId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }
}
