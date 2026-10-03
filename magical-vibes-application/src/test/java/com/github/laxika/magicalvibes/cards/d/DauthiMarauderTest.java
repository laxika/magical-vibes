package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauthiMarauder.class, MoggFanatic.class, SoltariFootSoldier.class})
class DauthiMarauderTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutShadow() {
        addCreatureReady(player1, new DauthiMarauder());
        addCreatureReady(player2, new MoggFanatic());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    void cannotBlockCreatureWithoutShadow() {
        addCreatureReady(player1, new MoggFanatic());
        addCreatureReady(player2, new DauthiMarauder());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    void canBeBlockedByCreatureWithShadow() {
        addCreatureReady(player1, new DauthiMarauder());
        Permanent blocker = addCreatureReady(player2, new SoltariFootSoldier());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockCreatureWithShadow() {
        addCreatureReady(player1, new SoltariFootSoldier());
        Permanent blocker = addCreatureReady(player2, new DauthiMarauder());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void dealsCombatDamageWhenUnblocked() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DauthiMarauder());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
