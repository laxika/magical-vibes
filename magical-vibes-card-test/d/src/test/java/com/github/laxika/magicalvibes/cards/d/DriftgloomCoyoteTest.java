package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
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

@CardUsed({DriftgloomCoyote.class, GrizzlyBears.class, HillGiant.class, Unsummon.class,
        VorinclexMonstrousRaider.class})
class DriftgloomCoyoteTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a small opposing creature and gets a +1/+1 counter")
    void exilesSmallCreatureAndGetsCounter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castCoyote(harness.getPermanentId(player2, "Grizzly Bears"));
        resolveAllTriggers();

        Permanent coyote = findPermanent(player1, "Driftgloom Coyote");
        assertThat(coyote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles a creature with power greater than 2 without getting a counter")
    void exilesLargeCreatureWithoutCounter() {
        harness.addToBattlefield(player2, new HillGiant());
        castCoyote(harness.getPermanentId(player2, "Hill Giant"));
        resolveAllTriggers();

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
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Driftgloom Coyote");
    }

    @Test
    @DisplayName("The exiled creature returns when the Coyote leaves")
    void exiledCreatureReturnsWhenSourceLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID originalId = harness.getPermanentId(player2, "Grizzly Bears");
        castCoyote(originalId);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Driftgloom Coyote"));
        resolveAllTriggers();

        harness.assertInHand(player1, "Driftgloom Coyote");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(originalId);
    }

    @Test
    @DisplayName("An invalid target prevents both exile and the counter")
    void targetLeavingBeforeResolutionPreventsCounter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castCoyote(targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, targetId);
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Driftgloom Coyote")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Uses the target's power at resolution rather than when the ability triggered")
    void targetPowerIncreasingBeforeResolutionPreventsCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCoyote(bears.getId());
        harness.passBothPriorities();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Driftgloom Coyote")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Exiles a small Vorinclex before placing the counter")
    void exilesCounterReplacementSourceBeforePuttingCounter() {
        Permanent vorinclex = harness.addToBattlefieldAndReturn(player2, new VorinclexMonstrousRaider());
        vorinclex.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        castCoyote(vorinclex.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Vorinclex, Monstrous Raider");
        assertThat(findPermanent(player1, "Driftgloom Coyote")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castCoyote(UUID targetId) {
        harness.setHand(player1, List.of(new DriftgloomCoyote()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
