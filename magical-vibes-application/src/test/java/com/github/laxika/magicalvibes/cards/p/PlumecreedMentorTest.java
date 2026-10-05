package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArchetypeOfImagination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JollyGerbils;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlumecreedMentor.class, GrizzlyBears.class, SerraAngel.class,
        JollyGerbils.class, ArchetypeOfImagination.class})
class PlumecreedMentorTest extends BaseCardTest {

    @Test
    void ownEntryPutsCounterOnTargetCreatureWithoutFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new PlumecreedMentor(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void anotherFlyingCreatureEntryPutsCounterOnTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player1, new PlumecreedMentor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new SerraAngel(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonFlyingCreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new PlumecreedMentor());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetCreatureWithFlying() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new PlumecreedMentor(), "{1}{W}{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void ownEntryStillTriggersWhenFlyingIsRemoved() {
        harness.addToBattlefield(player2, new ArchetypeOfImagination());
        Permanent gerbils = harness.addToBattlefieldAndReturn(player1, new JollyGerbils());

        harness.castFromHand(player1, new PlumecreedMentor(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, gerbils.getId());
        harness.passBothPriorities();

        assertThat(gerbils.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void anotherMentorWithoutFlyingDoesNotTriggerExistingMentor() {
        harness.addToBattlefield(player2, new ArchetypeOfImagination());
        harness.addToBattlefield(player1, new PlumecreedMentor());
        Permanent gerbils = harness.addToBattlefieldAndReturn(player1, new JollyGerbils());

        harness.castFromHand(player1, new PlumecreedMentor(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, gerbils.getId());
        harness.passBothPriorities();

        assertThat(gerbils.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentFlyingCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new PlumecreedMentor());
        Permanent gerbils = harness.addToBattlefieldAndReturn(player1, new JollyGerbils());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new PlumecreedMentor(), "{1}{W}{U}");
        harness.passBothPriorities();

        assertThat(gerbils.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentCreatureWithoutFlying() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new JollyGerbils());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new JollyGerbils());
        harness.castFromHand(player1, new PlumecreedMentor(), "{1}{W}{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void targetGainingFlyingBeforeResolutionGetsNoCounter() {
        Permanent gerbils = harness.addToBattlefieldAndReturn(player1, new JollyGerbils());
        harness.castFromHand(player1, new PlumecreedMentor(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, gerbils.getId());
        harness.addToBattlefield(player1, new ArchetypeOfImagination());
        harness.passBothPriorities();

        assertThat(gerbils.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canCastAndResolveWithoutAnyLegalTriggerTarget() {
        harness.castFromHand(player1, new PlumecreedMentor(), "{1}{W}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plumecreed Mentor");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
