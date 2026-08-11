package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HighPerfectMorcantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering itself makes each opponent blight a creature")
    void enteringItselfMakesOpponentBlight() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new HighPerfectMorcant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Tapping three Elves allows proliferating")
    void tapThreeElvesProliferates() {
        Permanent morcant = addReadyMorcant(player1);
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        elf.setSummoningSick(false);
        Permanent secondElf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        secondElf.setSummoningSick(false);
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
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        elf.setSummoningSick(false);
        Permanent secondElf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        secondElf.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(morcant);

        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMorcant(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new HighPerfectMorcant());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
