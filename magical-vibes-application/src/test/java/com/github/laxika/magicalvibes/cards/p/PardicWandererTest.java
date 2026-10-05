package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CabalEvangel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PardicWanderer.class, CabalEvangel.class})
class PardicWandererTest extends BaseCardTest {

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        Permanent attacker = addCreatureReady(player1, new PardicWanderer());
        Permanent blocker = addCreatureReady(player2, new CabalEvangel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Cabal Evangel");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void mayAssignAllDamageToBlocker() {
        addCreatureReady(player1, new PardicWanderer());
        Permanent blocker = addCreatureReady(player2, new CabalEvangel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Cabal Evangel");
    }

    @Test
    void cannotAssignPlayerDamageBeforeLethalDamageToBlocker() {
        addCreatureReady(player1, new PardicWanderer());
        Permanent blocker = addCreatureReady(player2, new CabalEvangel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must assign at least");
        harness.assertLife(player2, 20);
        assertThat(blocker.getMarkedDamage()).isZero();

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 3));
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Cabal Evangel");
    }

    @Test
    void mustAssignLethalDamageToEveryBlockerBeforeTramplingOver() {
        Permanent attacker = addCreatureReady(player1, new PardicWanderer());
        Permanent firstBlocker = addCreatureReady(player2, new CabalEvangel());
        Permanent secondBlocker = addCreatureReady(player2, new CabalEvangel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 1, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must assign at least");

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstBlocker, secondBlocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }
}
