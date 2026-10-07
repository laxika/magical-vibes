package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheThing.class, BurstOfStrength.class, FountainOfYouth.class, GrizzlyBears.class})
class TheThingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts four +1/+1 counters on itself at beginning of combat after a noncreature spell")
    void putsCountersAfterCastingNoncreatureSpell() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not put counters on itself when only a creature spell was cast")
    void doesNotPutCountersAfterCastingCreatureSpell() {
        Permanent thing = addCreatureReady(player1, new TheThing());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paying the attack trigger doubles every counter kind on chosen controlled permanents")
    void payingAttackTriggerDoublesCountersOnChosenPermanents() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bear.setCounterCount(CounterType.CHARGE, 3);
        fountain.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(indexOf(thing)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId(), fountain.getId())));

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(fountain.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bear.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(fountain.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger cannot target an opponent's permanent")
    void cannotTargetOpponentPermanent() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        addAttackPaymentMana();

        declareAttackers(List.of(indexOf(thing)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(opponentPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger asks for payment before any targets are chosen")
    void paymentPrecedesTargetSelection() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        thing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addAttackPaymentMana();

        declareAttackers(List.of(indexOf(thing)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(thing.getId())));

        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining payment does not choose targets or change counters")
    void decliningPaymentLeavesCountersUnchanged() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        thing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addAttackPaymentMana();

        declareAttackers(List.of(indexOf(thing)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("After paying, the controller may choose zero targets")
    void canPayAndChooseNoTargets() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        thing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addAttackPaymentMana();

        declareAttackers(List.of(indexOf(thing)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Beginning of combat does not trigger if no spell was cast")
    void noSpellDoesNotTrigger() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple noncreature spells still give only four counters per combat")
    void multipleNoncreatureSpellsGiveFourCounters() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FountainOfYouth(), new FountainOfYouth()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a noncreature spell after combat begins does not retroactively trigger")
    void noncreatureSpellCastDuringBeginningOfCombatIsTooLate() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT,
                () -> harness.castAndResolveInstant(player1, 0, thing.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Players can respond after payment and doubling uses the counters at resolution")
    void canRespondToReflexiveTrigger() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new BurstOfStrength()));
        addAttackPaymentMana();
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackers(List.of(indexOf(thing)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId())));

        harness.castAndResolveInstant(player1, 0, bear.getId());
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not satisfy the combat condition")
    void opponentNoncreatureSpellDoesNotSatisfyCondition() {
        Permanent thing = addCreatureReady(player1, new TheThing());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, thing.getId());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(thing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addAttackPaymentMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
