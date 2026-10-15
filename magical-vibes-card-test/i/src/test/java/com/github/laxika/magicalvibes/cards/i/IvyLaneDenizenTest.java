package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IvyLaneDenizen.class, GrizzlyBears.class, SavannahLions.class})
class IvyLaneDenizenTest extends BaseCardTest {

    @Test
    @DisplayName("Another green creature entering puts a +1/+1 counter on target creature")
    void greenCreatureEnteringPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new IvyLaneDenizen());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new SavannahLions());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recipient)).isEqualTo(2);
    }

    @Test
    @DisplayName("The counter may be placed on a creature an opponent controls")
    void counterCanGoOnOpponentCreature() {
        harness.addToBattlefield(player1, new IvyLaneDenizen());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A nongreen creature entering does not trigger the counter ability")
    void nongreenCreatureEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new IvyLaneDenizen());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new SavannahLions(), "{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, recipient)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ivy Lane Denizen does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new IvyLaneDenizen(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's green creature entering does not trigger")
    void opponentGreenCreatureDoesNotTrigger() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new IvyLaneDenizen());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(denizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The counter can be put on Ivy Lane Denizen itself")
    void counterCanGoOnDenizen() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new IvyLaneDenizen());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, denizen.getId());
        harness.passBothPriorities();

        assertThat(denizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entering creature can receive the counter")
    void enteringCreatureCanReceiveCounter() {
        harness.addToBattlefield(player1, new IvyLaneDenizen());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.handlePermanentChosen(player1, entering.getId());
        harness.passBothPriorities();

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A second Denizen triggers the first but does not trigger itself")
    void secondDenizenTriggersOnlyTheFirst() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IvyLaneDenizen());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new IvyLaneDenizen());

        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
