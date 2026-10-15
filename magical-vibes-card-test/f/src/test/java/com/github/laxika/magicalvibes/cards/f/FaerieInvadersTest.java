package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.s.SentinelSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaerieInvaders.class, KrakenHatchling.class, SentinelSpider.class})
class FaerieInvadersTest extends BaseCardTest {

    @Test
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new FaerieInvaders(), "{4}{U}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Faerie Invaders");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFlashInAfterAttackersAreDeclaredAndBlockImmediately() {
        addCreatureReady(player1, new KrakenHatchling());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        harness.castFromHand(player2, new FaerieInvaders(), "{4}{U}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Faerie Invaders");

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Kraken Hatchling");
        harness.assertOnBattlefield(player2, "Faerie Invaders");
        harness.assertLife(player2, 20);
    }

    @Test
    void groundCreatureCannotBlockFlyingAttacker() {
        addCreatureReady(player1, new FaerieInvaders());
        harness.addToBattlefield(player2, new KrakenHatchling());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockAndBothDieFromCombatDamage() {
        addCreatureReady(player1, new FaerieInvaders());
        harness.addToBattlefield(player2, new FaerieInvaders());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Faerie Invaders");
        harness.assertInGraveyard(player2, "Faerie Invaders");
        harness.assertLife(player2, 20);
    }

    @Test
    void reachCreatureCanBlockFlyingAttacker() {
        addCreatureReady(player1, new FaerieInvaders());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SentinelSpider());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        harness.assertInGraveyard(player1, "Faerie Invaders");
        harness.assertOnBattlefield(player2, "Sentinel Spider");
        harness.assertLife(player2, 20);
    }
}
