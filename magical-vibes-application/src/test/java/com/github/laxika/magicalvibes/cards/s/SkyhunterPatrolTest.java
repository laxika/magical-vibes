package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinReplica;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyhunterPatrol.class, GoblinReplica.class, SomberHoverguard.class})
class SkyhunterPatrolTest extends BaseCardTest {

    @Test
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SkyhunterPatrol());
        addCreatureReady(player2, new GoblinReplica());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void firstStrikeDealsDamageBeforeNonFirstStrikeCreature() {
        Permanent attacker = addCreatureReady(player1, new SkyhunterPatrol());
        Permanent blocker = addCreatureReady(player2, new SomberHoverguard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Somber Hoverguard");
    }
}
