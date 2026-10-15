package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({WandertaleMentor.class, Shock.class})
class WandertaleMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        Permanent mentor = addCreatureReady(player1, new WandertaleMentor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(mentor.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Taps for green mana")
    void tapsForGreenMana() {
        Permanent mentor = addCreatureReady(player1, new WandertaleMentor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(mentor.isTapped()).isTrue();
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller expends four")
    void putsCounterOnItselfWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WandertaleMentor(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent mentor = findPermanent(player1, "Wandertale Mentor");

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself before its controller expends four")
    void doesNotPutCounterOnItselfBelowExpendThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WandertaleMentor(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent mentor = findPermanent(player1, "Wandertale Mentor");

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Crossing four mana triggers only the Mentors already on the battlefield")
    void crossingThresholdTriggersOnlyExistingMentorsOnce() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent original = harness.addToBattlefieldAndReturn(player1, new WandertaleMentor());
        harness.setHand(player1, List.of(new WandertaleMentor(), new WandertaleMentor(), new WandertaleMentor()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        Permanent second = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        Permanent third = gd.playerBattlefields.get(player1.getId()).get(2);
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent expending four does not trigger the Mentor")
    void opponentSpendingDoesNotTriggerMentor() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new WandertaleMentor());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveInstant(player2, 0, player1.getId());
        }

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
