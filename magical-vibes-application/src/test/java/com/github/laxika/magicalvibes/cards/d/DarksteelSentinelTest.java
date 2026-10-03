package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.cards.r.RevokeExistence;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelSentinel.class, Shatter.class, GraspOfDarkness.class, RevokeExistence.class})
class DarksteelSentinelTest extends BaseCardTest {

    @Test
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new DarksteelSentinel(), "{6}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Darksteel Sentinel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastDuringCombatAndBlockImmediately() {
        addCreatureReady(player1, new DarksteelSentinel());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new DarksteelSentinel(), "{6}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Darksteel Sentinel");
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Darksteel Sentinel");
    }

    @Test
    void attackingDoesNotTapSentinel() {
        Permanent sentinel = addCreatureReady(player1, new DarksteelSentinel());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(sentinel.isTapped()).isFalse();
        harness.assertLife(player2, 17);
    }

    @Test
    void survivesLethalCombatDamageAsAttackerAndBlocker() {
        Permanent attacker = addCreatureReady(player1, new DarksteelSentinel());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DarksteelSentinel());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Darksteel Sentinel");
        harness.assertOnBattlefield(player2, "Darksteel Sentinel");
        harness.assertNotInGraveyard(player1, "Darksteel Sentinel");
        harness.assertNotInGraveyard(player2, "Darksteel Sentinel");
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotBeDestroyedByShatter() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new DarksteelSentinel());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, sentinel.getId());

        harness.assertOnBattlefield(player2, "Darksteel Sentinel");
        harness.assertNotInGraveyard(player2, "Darksteel Sentinel");
        harness.assertInGraveyard(player1, "Shatter");
    }

    @Test
    void diesWhenToughnessIsReducedBelowZero() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new DarksteelSentinel());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, sentinel.getId());

        harness.assertNotOnBattlefield(player2, "Darksteel Sentinel");
        harness.assertInGraveyard(player2, "Darksteel Sentinel");
    }

    @Test
    void indestructibleDoesNotPreventExile() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new DarksteelSentinel());
        harness.setHand(player1, List.of(new RevokeExistence()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, sentinel.getId());

        harness.assertNotOnBattlefield(player2, "Darksteel Sentinel");
        harness.assertNotInGraveyard(player2, "Darksteel Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(sentinel.getCard().getId()));
    }
}
