package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.ThunderousWrath;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoundOfGriselbrand.class, ThunderousWrath.class, PillarOfFlame.class})
class HoundOfGriselbrandTest extends BaseCardTest {

    @Test
    void unblockedHoundDealsDamageInBothSteps() {
        harness.setLife(player2, 20);
        Permanent hound = addCreatureReady(player1, new HoundOfGriselbrand());
        hound.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    void undyingReturnsANewPermanentWithOneCounter() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new HoundOfGriselbrand());
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, hound.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Hound of Griselbrand");
        assertThat(returned.getId()).isNotEqualTo(hound.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(returned.isSummoningSick()).isTrue();
        harness.assertNotInGraveyard(player1, "Hound of Griselbrand");
    }

    @Test
    void returnedHoundStaysDeadWhenKilledAgain() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new HoundOfGriselbrand());
        harness.setHand(player1, List.of(new ThunderousWrath(), new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castAndResolveInstant(player1, 0, hound.getId());
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Hound of Griselbrand");
        harness.castAndResolveInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hound of Griselbrand");
        harness.assertInGraveyard(player1, "Hound of Griselbrand");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exileInsteadOfDeathDoesNotTriggerUndying() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new HoundOfGriselbrand());
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, hound.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hound of Griselbrand");
        harness.assertNotInGraveyard(player1, "Hound of Griselbrand");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(hound.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void houndsKilledInFirstStrikeReturnOutsideCombat() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HoundOfGriselbrand());
        harness.addToBattlefield(player2, new HoundOfGriselbrand());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        for (var player : List.of(player1, player2)) {
            Permanent returned = findPermanent(player, "Hound of Griselbrand");
            assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(returned.isAttacking()).isFalse();
            harness.assertNotInGraveyard(player, "Hound of Griselbrand");
        }
        harness.assertLife(player2, 20);
    }
}
