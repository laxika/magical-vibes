package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarMachineJamesRhodes.class, GrizzlyBears.class, Forest.class})
class WarMachineJamesRhodesTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps up to one target creature")
    void attackTriggerTapsTargetCreature() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineJamesRhodes());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warMachine)));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking can choose no creature")
    void attackTriggerCanChooseNoTarget() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineJamesRhodes());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warMachine)));
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target a noncreature permanent")
    void attackTriggerRejectsNoncreatureTarget() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineJamesRhodes());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warMachine)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger can tap a creature its controller controls")
    void attackTriggerCanTapFriendlyCreature() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineJamesRhodes());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warMachine)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("War Machine can target itself even though attacking already tapped it")
    void attackTriggerCanTargetItself() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineJamesRhodes());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warMachine)));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(warMachine.getId());
        harness.handlePermanentChosen(player1, warMachine.getId());
        harness.passBothPriorities();

        assertThat(warMachine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger may target an already tapped creature")
    void attackTriggerCanTargetTappedCreature() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineJamesRhodes());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warMachine)));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger does not tap a replacement when its target leaves")
    void attackTriggerDoesNotRetarget() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineJamesRhodes());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warMachine)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.setHand(player2, List.of());
        Permanent replacement = addCreatureReady(player2, target.getCard());
        harness.passBothPriorities();

        assertThat(replacement.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger still resolves after War Machine leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineJamesRhodes());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warMachine)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, warMachine);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger War Machine")
    void anotherCreatureAttackingDoesNotTrigger() {
        addCreatureReady(player1, new WarMachineJamesRhodes());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(target.isTapped()).isFalse();
    }
}
