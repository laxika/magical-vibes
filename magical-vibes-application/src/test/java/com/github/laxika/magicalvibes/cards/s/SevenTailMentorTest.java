package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FuneralLongboat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SevenTailMentor.class, GrizzlyBears.class, FuneralLongboat.class, Forest.class, Assassinate.class})
class SevenTailMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield trigger targets a creature or Vehicle you control")
    void entersTargetsCreatureOrVehicleYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FuneralLongboat());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new SevenTailMentor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mentor = findPermanent(player1, "Seven-Tail Mentor");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), vehicle.getId(), mentor.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Its death trigger puts a +1/+1 counter on a target Vehicle you control")
    void diesPutsCounterOnTargetVehicleYouControl() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SevenTailMentor());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FuneralLongboat());
        mentor.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, mentor.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
