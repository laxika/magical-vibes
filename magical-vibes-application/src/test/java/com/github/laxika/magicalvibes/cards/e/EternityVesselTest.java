package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EternityVesselTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with charge counters equal to its controller's life total")
    void entersWithChargeCountersEqualToLifeTotal() {
        harness.setLife(player1, 17);
        harness.setHand(player1, List.of(new EternityVessel()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent vessel = findPermanent(player1, "Eternity Vessel");
        assertThat(vessel.getCounterCount(CounterType.CHARGE)).isEqualTo(17);
    }

    @Test
    @DisplayName("Landfall may set its controller's life total to its charge counters")
    void landfallMaySetLifeTotalToChargeCounters() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new EternityVessel());
        vessel.setCounterCount(CounterType.CHARGE, 12);
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Landfall does nothing when its controller declines")
    void landfallDoesNothingWhenDeclined() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new EternityVessel());
        vessel.setCounterCount(CounterType.CHARGE, 12);
        harness.setLife(player1, 5);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 5);
    }
}
