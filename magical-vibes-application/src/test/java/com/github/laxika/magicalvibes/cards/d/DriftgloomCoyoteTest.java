package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DriftgloomCoyote.class, GrizzlyBears.class, HillGiant.class, Unsummon.class})
class DriftgloomCoyoteTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a small opposing creature and gets a +1/+1 counter")
    void exilesSmallCreatureAndGetsCounter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castCoyote(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent coyote = findPermanent(player1, "Driftgloom Coyote");
        assertThat(coyote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles a creature with power greater than 2 without getting a counter")
    void exilesLargeCreatureWithoutCounter() {
        harness.addToBattlefield(player2, new HillGiant());
        castCoyote(harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent coyote = findPermanent(player1, "Driftgloom Coyote");
        assertThat(coyote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does not exile the target if the Coyote leaves before its ability resolves")
    void sourceLeavingBeforeResolutionStopsExile() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castCoyote(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        UUID coyoteId = harness.getPermanentId(player1, "Driftgloom Coyote");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, coyoteId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Driftgloom Coyote");
    }

    private void castCoyote(UUID targetId) {
        harness.setHand(player1, List.of(new DriftgloomCoyote()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
