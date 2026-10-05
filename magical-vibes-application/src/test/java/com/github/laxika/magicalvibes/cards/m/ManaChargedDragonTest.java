package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaChargedDragon.class, GrizzlyBears.class, Mountain.class})
class ManaChargedDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may pay mana and the Dragon gets the shared total as power")
    void attackTriggerUsesTotalManaPaid() {
        Permanent dragon = addCreatureReady(player1, new ManaChargedDragon());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleXValueChosen(player2, 1);

        assertThat(dragon.getPowerModifier()).isEqualTo(3);
        assertThat(dragon.getToughnessModifier()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(dragon.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The block trigger starts with the Dragon's controller")
    void blockTriggerStartsWithController() {
        Permanent dragon = addCreatureReady(player1, new ManaChargedDragon());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.RED, 2);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleXValueChosen(player1, 2);

        assertThat(dragon.getPowerModifier()).isEqualTo(2);
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Zero payments leave the Dragon unchanged")
    void zeroPaymentsLeaveDragonUnchanged() {
        Permanent dragon = addCreatureReady(player1, new ManaChargedDragon());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The controller may decline while the opponent contributes")
    void controllerCanDeclinePayment() {
        Permanent dragon = addCreatureReady(player1, new ManaChargedDragon());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleXValueChosen(player2, 3);

        assertThat(dragon.getPowerModifier()).isEqualTo(3);
        assertThat(dragon.getToughnessModifier()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Both players may decline despite having available mana")
    void bothPlayersCanDeclinePayment() {
        Permanent dragon = addCreatureReady(player1, new ManaChargedDragon());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(dragon.getToughnessModifier()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The second player's blocking Dragon asks its controller before the active player")
    void secondPlayerBlockTriggerStartsWithController() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent dragon = addCreatureReady(player2, new ManaChargedDragon());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleXValueChosen(player2, 2);
        assertThat(dragon.getPowerModifier()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleXValueChosen(player1, 1);

        assertThat(dragon.getPowerModifier()).isEqualTo(3);
        assertThat(dragon.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Both players can tap lands to contribute during resolution")
    void playersCanGenerateManaDuringResolution() {
        Permanent dragon = addCreatureReady(player1, new ManaChargedDragon());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.tapPermanent(player1, 1);
        harness.handleXValueChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.tapPermanent(player2, 0);
        harness.handleXValueChosen(player2, 1);

        assertThat(dragon.getPowerModifier()).isEqualTo(2);
        assertThat(dragon.getToughnessModifier()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
