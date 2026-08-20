package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HostOfTheHereafterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent host = addCreatureReady(player1, new HostOfTheHereafter());

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another creature's counters move to a creature you control when it dies")
    void anotherCreatureDeathMovesCountersToControlledCreature() {
        Permanent host = addCreatureReady(player1, new HostOfTheHereafter());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        dyingCreature.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyWithAssassinateFromPlayerTwo(dyingCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, host.getId());
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("The death trigger only offers creatures you control")
    void deathTriggerTargetsOnlyControlledCreatures() {
        addCreatureReady(player1, new HostOfTheHereafter());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dyingCreature.tap();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithAssassinateFromPlayerTwo(dyingCreature.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature without counters does not trigger the counter transfer")
    void creatureWithoutCountersDoesNotTrigger() {
        addCreatureReady(player1, new HostOfTheHereafter());
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        dyingCreature.tap();

        destroyWithAssassinateFromPlayerTwo(dyingCreature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("When Host of the Hereafter dies, its counters move to a creature you control")
    void ownDeathMovesCountersToControlledCreature() {
        Permanent host = addCreatureReady(player1, new HostOfTheHereafter());
        host.tap();
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithAssassinateFromPlayerTwo(host.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void destroyWithAssassinateFromPlayerTwo(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, targetId, null);
        harness.passBothPriorities();
    }
}
