package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.r.RakdosTrumpeter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CultGuildmage.class, RakdosTrumpeter.class, DovinGrandArbiter.class})
class CultGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Black ability makes the target player discard a card")
    void targetPlayerDiscards() {
        addReadyGuildmage();
        harness.setHand(player2, List.of(new RakdosTrumpeter()));
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Rakdos Trumpeter");
    }

    @Test
    @DisplayName("Black ability can only be activated at sorcery speed")
    void discardAbilityRequiresSorcerySpeed() {
        addReadyGuildmage();
        harness.setHand(player2, List.of(new RakdosTrumpeter()));
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Red ability deals 1 damage to a target opponent")
    void damagesOpponent() {
        addReadyGuildmage();
        harness.setLife(player2, 20);
        addMana(ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Red ability can deal damage to an opposing planeswalker")
    void damagesPlaneswalker() {
        addReadyGuildmage();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addMana(ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Red ability cannot target its controller or a creature")
    void redAbilityRequiresOpponentOrPlaneswalker() {
        addReadyGuildmage();
        addMana(ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RakdosTrumpeter());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardCanTargetControllerAndPlayerChoosesCard() {
        addReadyGuildmage();
        harness.setHand(player1, List.of(new RakdosTrumpeter(), new CultGuildmage()));
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Cult Guildmage");
    }

    @Test
    void discardWithEmptyHandStillPaysTapCost() {
        Permanent guildmage = addCreatureReady(player1, new CultGuildmage());
        harness.setHand(player2, List.of());
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        assertThat(guildmage.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void redAbilityCanTargetOwnPlaneswalkerDuringOpponentsTurn() {
        Permanent guildmage = addCreatureReady(player1, new CultGuildmage());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        addMana(ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, planeswalker.getId());
        assertThat(guildmage.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void discardCannotBeActivatedOutsideMainPhase() {
        addReadyGuildmage();
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardCannotBeActivatedWithNonemptyStack() {
        addReadyGuildmage();
        addReadyGuildmage();
        addMana(ManaColor.RED, 1);
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    @Test
    void bothAbilitiesRequireCreatureToBeReadyAndUntapped() {
        Permanent guildmage = addCreatureReady(player1, new CultGuildmage());
        addMana(ManaColor.RED, 1);
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        guildmage.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        guildmage.setSummoningSick(false);
        guildmage.setTapped(true);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyGuildmage() {
        addCreatureReady(player1, new CultGuildmage());
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }
}
