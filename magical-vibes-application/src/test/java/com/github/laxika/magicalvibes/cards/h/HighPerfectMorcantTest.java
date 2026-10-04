package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProwessOfTheFair;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighPerfectMorcant.class, ElvishWarrior.class, GrizzlyBears.class,
        ProwessOfTheFair.class, Conspiracy.class})
class HighPerfectMorcantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering itself makes each opponent blight a creature")
    void enteringItselfMakesOpponentBlight() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new HighPerfectMorcant(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, secondCreature.getId());

        assertThat(firstCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(secondCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another Elf entering also triggers blight")
    void anotherElfEnteringTriggersBlight() {
        addReadyMorcant(player1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ElvishWarrior(), "{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Elf entering does not trigger blight")
    void nonElfDoesNotTriggerBlight() {
        addReadyMorcant(player1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Tapping three Elves allows proliferating")
    void tapThreeElvesProliferates() {
        Permanent morcant = addReadyMorcant(player1);
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        Permanent secondElf = addCreatureReady(player1, new ElvishWarrior());
        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(morcant);
        harness.activateAbility(player1, sourceIndex, 0, null, null);

        assertThat(morcant.isTapped()).isTrue();
        assertThat(elf.isTapped()).isTrue();
        assertThat(secondElf.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The proliferate ability cannot be activated outside a main phase")
    void proliferateAbilityRequiresSorcerySpeed() {
        Permanent morcant = addReadyMorcant(player1);
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(morcant);

        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringItselfTriggersEvenWhenItsCreatureTypeIsReplaced() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new HighPerfectMorcant(), "{2}{B}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void noncreatureElfEnteringTriggersBlight() {
        addReadyMorcant(player1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new ProwessOfTheFair(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsElfEnteringDoesNotTriggerBlight() {
        Permanent morcant = addReadyMorcant(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new ElvishWarrior(), "{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(morcant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void opponentWithoutCreaturesDoesNotBlightControllersCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new HighPerfectMorcant(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void summoningSickElvesCanPayTapCost() {
        Permanent morcant = harness.addToBattlefieldAndReturn(player1, new HighPerfectMorcant());
        Permanent firstElf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent secondElf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(morcant.isTapped()).isTrue();
        assertThat(firstElf.isTapped()).isTrue();
        assertThat(secondElf.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappedElvesNonElvesAndOpponentsElvesCannotPayCost() {
        Permanent morcant = addReadyMorcant(player1);
        Permanent tappedElf = addCreatureReady(player1, new ElvishWarrior());
        tappedElf.tap();
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new ElvishWarrior());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(morcant.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferateAddsEveryExistingCounterKindToSelectedPermanentsAndPlayers() {
        addReadyMorcant(player1);
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent selected = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        selected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        selected.setCounterCount(CounterType.CHARGE, 1);
        Permanent unselected = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        unselected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId(), player2.getId()));

        assertThat(selected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(selected.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void proliferateMayChooseNothing() {
        addReadyMorcant(player1);
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void proliferateCannotBeActivatedWithSpellOnStack() {
        Permanent morcant = addReadyMorcant(player1);
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(morcant.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyMorcant(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new HighPerfectMorcant());
    }
}
