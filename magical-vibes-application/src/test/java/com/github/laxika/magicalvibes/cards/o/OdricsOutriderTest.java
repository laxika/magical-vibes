package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BurnDownTheHouse;
import com.github.laxika.magicalvibes.cards.g.GavonyTrapper;
import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OdricsOutrider.class, InfernalGrasp.class, GavonyTrapper.class, BurnDownTheHouse.class})
class OdricsOutriderTest extends BaseCardTest {

    @Test
    void putsCounterOnTargetCreatureWhenAnotherCreatureYouControlDies() {
        harness.addToBattlefield(player1, new OdricsOutrider());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());

        destroyWithInfernalGraspFromPlayerTwo(dyingCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnTargetCreatureWhenThisCreatureDies() {
        Permanent outrider = harness.addToBattlefieldAndReturn(player1, new OdricsOutrider());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());

        destroyWithInfernalGraspFromPlayerTwo(outrider.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void deathTriggerOnlyOffersCreaturesYouControl() {
        harness.addToBattlefield(player1, new OdricsOutrider());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GavonyTrapper());

        destroyWithInfernalGraspFromPlayerTwo(dyingCreature.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).doesNotContain(opponentCreature.getId());
    }

    @Test
    void canPutCounterOnItselfWhenAnotherCreatureDies() {
        Permanent outrider = harness.addToBattlefieldAndReturn(player1, new OdricsOutrider());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());

        destroyWithInfernalGraspFromPlayerTwo(dyingCreature.getId());

        harness.handlePermanentChosen(player1, outrider.getId());
        harness.passBothPriorities();

        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenAnOpponentsCreatureDies() {
        Permanent outrider = harness.addToBattlefieldAndReturn(player1, new OdricsOutrider());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new GavonyTrapper());

        destroyWithInfernalGraspFromPlayerTwo(dyingCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Gavony Trapper");
    }

    @Test
    void deathWithNoLegalTargetDoesNotLeaveAnInteractionOrAbility() {
        Permanent outrider = harness.addToBattlefieldAndReturn(player1, new OdricsOutrider());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GavonyTrapper());

        destroyWithInfernalGraspFromPlayerTwo(outrider.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Odric's Outrider");
    }

    @Test
    void doesNotPutCounterOnATargetThatDiesBeforeResolution() {
        Permanent outrider = harness.addToBattlefieldAndReturn(player1, new OdricsOutrider());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());

        destroyWithInfernalGraspFromPlayerTwo(dyingCreature.getId());
        harness.handlePermanentChosen(player1, recipient.getId());

        destroyWithInfernalGraspFromPlayerTwo(recipient.getId());
        harness.handlePermanentChosen(player1, outrider.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForItselfAndEachAllyThatDiesSimultaneously() {
        harness.addToBattlefield(player1, new OdricsOutrider());
        harness.addToBattlefield(player1, new GavonyTrapper());
        harness.addToBattlefield(player1, new GavonyTrapper());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.addToBattlefield(player2, new GavonyTrapper());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new BurnDownTheHouse()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player2, 0, 0, List.of());
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, recipient.getId());
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(3);
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Odric's Outrider");
    }

    private void destroyWithInfernalGraspFromPlayerTwo(java.util.UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
