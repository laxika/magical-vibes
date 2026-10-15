package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FuneralLongboat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoaringEarth.class, Forest.class, GrizzlyBears.class, FuneralLongboat.class, BambooGroveArcher.class})
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

    @Test
    @DisplayName("Channel with zero X sends an otherwise unmodified land to the graveyard")
    void channelWithZeroXAnimatesLandAsZeroToughnessCreature() {
        harness.setHand(player1, List.of(new RoaringEarth()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, land.getId(), 0);
        harness.assertInGraveyard(player1, "Roaring Earth");
        harness.assertOnBattlefield(player1, "Forest");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Channel grants haste so a newly played land can still tap for mana")
    void channelGrantsHasteToNewLand() {
        harness.setHand(player1, List.of(new Forest(), new RoaringEarth()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Forest");
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, land.getId(), 1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        harness.tapPermanent(player1, 0);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel animation, Spirit subtype, color, and haste survive turn cleanup")
    void channelAnimationPersistsIntoNextTurn() {
        harness.setHand(player1, List.of(new RoaringEarth()));
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, land.getId(), 1);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.SPIRIT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, land)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's entering land does not trigger Roaring Earth")
    void opponentLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new RoaringEarth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Channel cannot target a nonland creature you control")
    void channelCannotTargetNonlandCreature() {
        harness.setHand(player1, List.of(new RoaringEarth()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, creature.getId(), 1))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Roaring Earth");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Channel with zero X preserves counters on a previously animated land")
    void channelAgainPreservesExistingCounters() {
        harness.setHand(player1, List.of(new RoaringEarth(), new RoaringEarth()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateHandAbility(player1, 0, land.getId(), 2);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, land.getId(), 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
    }
}
