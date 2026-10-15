package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThatcherRevolt;
import com.github.laxika.magicalvibes.cards.t.ThunderousWrath;
import com.github.laxika.magicalvibes.cards.t.TibaltTheFiendBlooded;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KessigMalcontents.class, DoomedTraveler.class, GrizzlyBears.class,
        ThatcherRevolt.class, ThunderousWrath.class, TibaltTheFiendBlooded.class})
class KessigMalcontentsTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new KessigMalcontents()));
        harness.addMana(player1, ManaColor.RED, 3); // {2}{R}
        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Deals damage equal to the number of Humans you control, counting itself")
    void countsItself() {
        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Counts other Humans you control")
    void countsOtherHumans() {
        harness.addToBattlefield(player1, new DoomedTraveler());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }

    @Test
    @DisplayName("Ignores opponents' Humans and your non-Humans")
    void ignoresOpponentHumansAndNonHumans() {
        harness.addToBattlefield(player2, new DoomedTraveler());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    void canTargetItsController() {
        castUntilTrigger(player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void damagesTargetPlaneswalkerWithoutDamagingItsController() {
        var tibalt = harness.addToBattlefieldAndReturn(player2, new TibaltTheFiendBlooded());
        tibalt.setCounterCount(CounterType.LOYALTY, 2);

        castUntilTrigger(tibalt.getId());
        resolveAllTriggers();

        assertThat(tibalt.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void countsHumanTokens() {
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        cast();

        harness.assertLife(player2, 16);
    }

    @Test
    void includesHumanAddedAfterTriggering() {
        castUntilTrigger(player2.getId());
        harness.assertLife(player2, 20);
        harness.addToBattlefield(player1, new KessigMalcontents());

        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void sourceLeavingDoesNotStopTriggerAndIsNotCounted() {
        castUntilTrigger(player2.getId());
        UUID sourceId = harness.getPermanentId(player1, "Kessig Malcontents");
        harness.addToBattlefield(player1, new KessigMalcontents());
        destroyInResponse(sourceId);

        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Kessig Malcontents");
    }

    @Test
    void dealsNoDamageWhenNoHumansRemainAtResolution() {
        castUntilTrigger(player2.getId());
        destroyInResponse(harness.getPermanentId(player1, "Kessig Malcontents"));

        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Kessig Malcontents");
        harness.assertInGraveyard(player1, "Kessig Malcontents");
    }

    private void castUntilTrigger(UUID targetId) {
        harness.setHand(player1, List.of(new KessigMalcontents()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Kessig Malcontents");
    }

    private void destroyInResponse(UUID targetId) {
        harness.setHand(player1, List.of(new ThunderousWrath()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
