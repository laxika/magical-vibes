package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenFleetwing.class, RuneclawBear.class, GiantSpider.class, Unsummon.class, DayOfJudgment.class})
class AvenFleetwingTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player2, new RuneclawBear());
        Permanent fleetwing = addCreatureReady(player1, new AvenFleetwing());
        fleetwing.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canBeBlockedByCreatureWithReach() {
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        Permanent fleetwing = addCreatureReady(player1, new AvenFleetwing());
        fleetwing.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByAnotherFlyingCreatureDespiteHexproof() {
        Permanent blocker = addCreatureReady(player2, new AvenFleetwing());
        Permanent attacker = addCreatureReady(player1, new AvenFleetwing());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void opponentCannotTargetWithUnsummon() {
        Permanent fleetwing = addCreatureReady(player1, new AvenFleetwing());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, fleetwing.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void controllerCanTargetWithUnsummon() {
        AvenFleetwing card = new AvenFleetwing();
        Permanent fleetwing = addCreatureReady(player1, card);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, fleetwing.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fleetwing);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    @Test
    void hexproofDoesNotPreventOpponentsUntargetedDestruction() {
        AvenFleetwing card = new AvenFleetwing();
        Permanent fleetwing = addCreatureReady(player1, card);
        harness.setHand(player2, List.of(new DayOfJudgment()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fleetwing);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }
}
