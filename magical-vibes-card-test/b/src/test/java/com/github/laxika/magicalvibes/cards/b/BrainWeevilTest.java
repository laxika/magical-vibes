package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrainWeevil.class, AvacynsPilgrim.class, ThinkTwice.class, Forest.class, OneEyedScarecrow.class})
class BrainWeevilTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Brain Weevil and puts discard on stack")
    void activateAbilitySacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new BrainWeevil());

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Brain Weevil");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ability causes target player to discard two cards")
    void targetDiscardsTwoCards() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.setHand(player2, List.of(new AvacynsPilgrim(), new ThinkTwice(), new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Brain Weevil goes to graveyard after sacrifice")
    void goesToGraveyardAfterSacrifice() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.setHand(player2, List.of(new AvacynsPilgrim(), new ThinkTwice()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Brain Weevil");
    }

    @Test
    @DisplayName("Target with empty hand results in no discard prompt")
    void targetWithEmptyHandNoPrompt() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target self with the ability")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.setHand(player1, List.of(new AvacynsPilgrim(), new ThinkTwice()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate at instant speed during opponent's turn")
    void cannotActivateAtInstantSpeed() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Target with only one card discards it and completes resolution")
    void targetWithOneCardDiscardsIt() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.setHand(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        var weevil = harness.addToBattlefieldAndReturn(player1, new BrainWeevil());
        weevil.tap();
        weevil.setSummoningSick(true);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Brain Weevil");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate in the postcombat main phase")
    void canActivateInPostcombatMainPhase() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Brain Weevil");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate during combat on the controller's turn")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Brain Weevil");
        harness.assertNotInGraveyard(player1, "Brain Weevil");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while another spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        harness.addToBattlefield(player1, new BrainWeevil());
        harness.castFromHand(player1, new ThinkTwice(), "{1}{U}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Brain Weevil");
        harness.assertNotInGraveyard(player1, "Brain Weevil");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Intimidate prevents a green nonartifact creature from blocking")
    void greenNonartifactCannotBlock() {
        addCreatureReady(player1, new BrainWeevil());
        var blocker = addCreatureReady(player2, new AvacynsPilgrim());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A black creature can block Brain Weevil")
    void blackCreatureCanBlock() {
        addCreatureReady(player1, new BrainWeevil());
        var blocker = addCreatureReady(player2, new BrainWeevil());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A colorless artifact creature can block Brain Weevil")
    void artifactCreatureCanBlock() {
        addCreatureReady(player1, new BrainWeevil());
        var blocker = addCreatureReady(player2, new OneEyedScarecrow());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
