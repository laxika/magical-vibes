package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DecreeOfSavagery.class, GrizzlyBears.class})
class DecreeOfSavageryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting it puts four +1/+1 counters on each creature you control")
    void castingPutsCountersOnEachControlledCreature() {
        Permanent ownFirst = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownSecond = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecreeOfSavagery()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownFirst.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ownSecond.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cycling may put four +1/+1 counters on any target creature and draws a card")
    void cyclingMayPutCountersOnTargetCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecreeOfSavagery()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Decree of Savagery");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the cycling trigger still draws a card")
    void decliningCyclingTriggerStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecreeOfSavagery()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Decree of Savagery");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling without a creature target still draws a card")
    void cyclingWithoutCreatureTargetStillDraws() {
        harness.setHand(player1, List.of(new DecreeOfSavagery()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Decree of Savagery");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
