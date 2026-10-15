package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AssassinsInk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.ImperialRecoveryUnit;
import com.github.laxika.magicalvibes.cards.p.PeerlessSamurai;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SevenTailMentor.class, PeerlessSamurai.class, ImperialRecoveryUnit.class, Forest.class, AssassinsInk.class})
class SevenTailMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield trigger targets a creature or Vehicle you control")
    void entersTargetsCreatureOrVehicleYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PeerlessSamurai());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ImperialRecoveryUnit());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PeerlessSamurai());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new ImperialRecoveryUnit());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new SevenTailMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mentor = findPermanent(player1, "Seven-Tail Mentor");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), vehicle.getId(), mentor.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId(), opponentVehicle.getId());

        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Its death trigger puts a +1/+1 counter on a target Vehicle you control")
    void diesPutsCounterOnTargetVehicleYouControl() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SevenTailMentor());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ImperialRecoveryUnit());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new AssassinsInk()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, mentor.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its enters trigger can put a counter on itself")
    void entersCanTargetItself() {
        harness.setHand(player1, List.of(new SevenTailMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mentor = findPermanent(player1, "Seven-Tail Mentor");
        harness.handlePermanentChosen(player1, mentor.getId());
        resolveAllTriggers();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its death trigger targets a surviving creature, not itself or opponents' permanents")
    void diesTargetsSurvivingCreatureYouControl() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SevenTailMentor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PeerlessSamurai());
        harness.addToBattlefield(player2, new PeerlessSamurai());
        harness.addToBattlefield(player2, new ImperialRecoveryUnit());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player2, List.of(new AssassinsInk()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, mentor.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Seven-Tail Mentor");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its enters trigger does nothing if its target leaves before resolution")
    void entersTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PeerlessSamurai());
        harness.setHand(player1, List.of(new SevenTailMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent mentor = findPermanent(player1, "Seven-Tail Mentor");
        harness.handlePermanentChosen(player1, creature.getId());

        harness.setHand(player2, List.of(new AssassinsInk()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Peerless Samurai");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its death trigger cannot target an opponent's permanent when no friendly target survives")
    void diesWithoutLegalTarget() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SevenTailMentor());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PeerlessSamurai());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new ImperialRecoveryUnit());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player2, List.of(new AssassinsInk()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, mentor.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Seven-Tail Mentor");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentVehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
