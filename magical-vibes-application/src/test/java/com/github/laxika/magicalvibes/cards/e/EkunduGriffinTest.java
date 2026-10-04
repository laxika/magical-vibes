package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EkunduGriffin.class, GrizzlyBears.class, WindDrake.class})
class EkunduGriffinTest extends BaseCardTest {

    @Test
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent griffin = addCreatureReady(player1, new EkunduGriffin());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(griffin)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void firstStrikeDealsCombatDamageFirst() {
        addCreatureReady(player1, new EkunduGriffin());
        addCreatureReady(player2, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player1, "Ekundu Griffin");
        harness.assertInGraveyard(player2, "Wind Drake");
        harness.assertLife(player2, 20);
    }

    @Test
    void firstStrikeKillsGroundAttackerBeforeItDealsDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new EkunduGriffin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ekundu Griffin");
        harness.assertLife(player2, 20);
    }
}
