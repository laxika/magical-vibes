package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({MoldAdder.class, FugitiveWizard.class, GrizzlyBears.class, ChildOfNight.class, Ponder.class})
class MoldAdderTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent casting a blue spell triggers may ability and accepting adds counter")
    void opponentBlueSpellAcceptedAddsCounter() {
        Permanent adder = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player2, 0);

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, adder)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, adder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent casting a black spell triggers may ability and accepting adds counter")
    void opponentBlackSpellAcceptedAddsCounter() {
        Permanent adder = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new ChildOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castCreature(player2, 0);

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);

        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining may ability does not add counter")
    void opponentBlueSpellDeclinedDoesNotAddCounter() {
        Permanent adder = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent casting a green spell does not trigger Mold Adder")
    void opponentGreenSpellDoesNotTrigger() {
        Permanent adder = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Controller casting a blue spell does not trigger Mold Adder")
    void controllerBlueSpellDoesNotTrigger() {
        Permanent adder = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Optional counter choice waits until the triggered ability resolves")
    void counterChoiceWaitsForResolution() {
        Permanent adder = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).hasSize(2);
        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A blue noncreature spell triggers before that spell resolves")
    void blueSorceryTriggers() {
        Permanent adder = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Ponder()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(adder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player2, "Ponder");
    }

    @Test
    @DisplayName("Each Mold Adder has its own optional counter trigger")
    void multipleAddersChooseIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MoldAdder());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(List.of(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE),
                second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))).containsExactlyInAnyOrder(1, 0);
        assertThat(gd.stack).hasSize(1);
    }
}
