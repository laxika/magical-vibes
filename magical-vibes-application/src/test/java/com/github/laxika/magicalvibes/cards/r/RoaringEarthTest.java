package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FuneralLongboat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoaringEarth.class, Forest.class, GrizzlyBears.class, FuneralLongboat.class})
class RoaringEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on a creature you control")
    void landfallPutsCounterOnCreatureYouControl() {
        harness.addToBattlefieldAndReturn(player1, new RoaringEarth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall can put a +1/+1 counter on a Vehicle you control")
    void landfallPutsCounterOnVehicleYouControl() {
        harness.addToBattlefieldAndReturn(player1, new RoaringEarth());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FuneralLongboat());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall does not target a creature controlled by an opponent")
    void landfallCannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new RoaringEarth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Channel puts X counters on a land and permanently animates it as a green Spirit")
    void channelAnimatesLandWithXCounters() {
        harness.setHand(player1, List.of(new RoaringEarth()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateHandAbility(player1, 0, land.getId(), 2);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, land)).containsExactly(CardColor.GREEN);
        harness.assertInGraveyard(player1, "Roaring Earth");
    }

    @Test
    @DisplayName("Channel cannot target an opponent's land")
    void channelCannotTargetOpponentLand() {
        harness.setHand(player1, List.of(new RoaringEarth()));
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, opponentLand.getId(), 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Roaring Earth");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
