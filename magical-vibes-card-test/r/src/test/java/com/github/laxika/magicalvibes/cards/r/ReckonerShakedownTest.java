package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.b.BruteSuit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MoonsnarePrototype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReckonerShakedown.class, Forest.class, BambooGroveArcher.class, MoonsnarePrototype.class, BruteSuit.class})
class ReckonerShakedownTest extends BaseCardTest {

    @Test
    void choosesNonlandCardToDiscard() {
        Permanent bears = addCreatureReady(player1, new BambooGroveArcher());
        harness.setHand(player2, List.of(new MoonsnarePrototype(), new Forest()));
        cast();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Moonsnare Prototype");
        harness.assertInHand(player2, "Forest");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void decliningChoicePutsCountersOnCreature() {
        Permanent bears = addCreatureReady(player1, new BambooGroveArcher());
        harness.setHand(player2, List.of(new MoonsnarePrototype()));
        cast();

        harness.handleCardChosen(player1, -1);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player2, "Moonsnare Prototype");
    }

    @Test
    void noNonlandCardUsesCounterModeWithoutPrompt() {
        Permanent bears = addCreatureReady(player1, new BambooGroveArcher());
        harness.setHand(player2, List.of(new Forest()));
        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player2, "Forest");
    }

    @Test
    void canPutCountersOnVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BruteSuit());
        harness.setHand(player2, List.of(new Forest()));
        cast();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canOnlyTargetOpponent() {
        harness.setHand(player1, List.of(new ReckonerShakedown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void emptyHandStillPutsCountersOnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player2, List.of());
        cast();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Reckoner Shakedown");
    }

    @Test
    void choosesOnlyOneOwnCreatureOrVehicleAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BruteSuit());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BambooGroveArcher());
        harness.setHand(player2, List.of(new MoonsnarePrototype()));
        cast();
        harness.handleCardChosen(player1, -1);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), vehicle.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(vehicle.getId()));

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInHand(player2, "Moonsnare Prototype");
        harness.assertInGraveyard(player1, "Reckoner Shakedown");
    }

    @Test
    void decliningWithoutEligiblePermanentDoesNotDiscard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MoonsnarePrototype());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BambooGroveArcher());
        harness.setHand(player2, List.of(new MoonsnarePrototype()));
        cast();
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player2, "Moonsnare Prototype");
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Reckoner Shakedown");
    }

    @Test
    void canDiscardOneNonlandWithoutControllingCreatureOrVehicle() {
        harness.setHand(player2, List.of(new Forest(), new MoonsnarePrototype(), new BruteSuit()));
        cast();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 2);

        harness.assertInGraveyard(player2, "Brute Suit");
        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Moonsnare Prototype");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Reckoner Shakedown");
    }

    private void cast() {
        harness.setHand(player1, List.of(new ReckonerShakedown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
