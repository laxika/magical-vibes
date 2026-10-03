package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TreeMonkey;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrimstoneDragon.class, BearCub.class, TreeMonkey.class})
class BrimstoneDragonTest extends BaseCardTest {

    @Test
    void canAttackOnTheTurnItIsCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BrimstoneDragon(), "{6}{R}{R}");
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Brimstone Dragon");
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(dragon.isAttacking()).isTrue();
        assertThat(dragon.isTapped()).isTrue();
    }

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new BrimstoneDragon());
        Permanent blocker = addCreatureReady(player2, new BearCub());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new BrimstoneDragon());
        Permanent blocker = addCreatureReady(player2, new BrimstoneDragon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new BrimstoneDragon());
        Permanent blocker = addCreatureReady(player2, new TreeMonkey());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockGroundCreatureEvenWhenSummoningSick() {
        addCreatureReady(player1, new BearCub());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new BrimstoneDragon());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(dragon.isBlocking()).isTrue();
    }
}
