package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LifecreedDuo;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalewindMoose.class, LifecreedDuo.class})
class GalewindMooseTest extends BaseCardTest {

    @Test
    void canCastDuringOpponentsTurnWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GalewindMoose(), "{4}{G}{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent moose = addCreatureReady(player1, new GalewindMoose());

        declareAttackers(List.of(0));

        assertThat(moose.isTapped()).isFalse();
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new LifecreedDuo());
        Permanent moose = addCreatureReady(player2, new GalewindMoose());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(moose.isBlocking()).isTrue();
        resolveCombat();
        harness.assertInGraveyard(player1, "Lifecreed Duo");
        harness.assertOnBattlefield(player2, "Galewind Moose");
        harness.assertLife(player2, 20);
    }

    @Test
    void trampleDealsDamageBeyondBlockersToughness() {
        Permanent moose = addCreatureReady(player1, new GalewindMoose());
        Permanent blocker = addCreatureReady(player2, new LifecreedDuo());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Lifecreed Duo");
        harness.assertOnBattlefield(player1, "Galewind Moose");
        assertThat(moose.isTapped()).isFalse();
    }
}
