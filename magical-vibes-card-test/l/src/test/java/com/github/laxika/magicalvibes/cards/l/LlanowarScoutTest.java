package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarScout.class, Forest.class, LlanowarElves.class})
class LlanowarScoutTest extends BaseCardTest {

    

    @Test
    @DisplayName("Activating ability taps Llanowar Scout and puts ability on stack")
    void activatingTapsAndUsesStack() {
        Permanent scout = addReadyScout(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(scout.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Llanowar Scout");
    }

    @Test
    @DisplayName("Resolving ability prompts may choice first")
    void resolvingPromptsMayChoice() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new Forest(), new LlanowarElves()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may prompt allows choosing only land cards from hand")
    void acceptingMayPromptsLandChoice() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new LlanowarElves(), new Forest(), new LlanowarElves()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Choosing a land puts it onto the battlefield untapped")
    void choosingLandPutsItOntoBattlefieldUntapped() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new Forest()));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ability can put a land onto battlefield even if a land was already played this turn")
    void canPutLandEvenAfterLandPlay() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new Forest()));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Declining may choice leaves hand unchanged")
    void decliningMayLeavesHandUnchanged() {
        addReadyScout(player1);
        harness.setHand(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Cannot activate while Llanowar Scout has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new LlanowarScout());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate while Llanowar Scout is tapped")
    void cannotActivateWhileTapped() {
        Permanent scout = addReadyScout(player1);
        scout.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Can activate during opponent's turn (instant speed)")
    void canActivateDuringOpponentsTurn() {
        addReadyScout(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate outside main phase (instant speed)")
    void canActivateOutsideMainPhase() {
        addReadyScout(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Accepting with no land in hand finishes without putting a card onto the battlefield")
    void acceptingWithNoLandDoesNothing() {
        addReadyScout(player1);
        LlanowarElves creature = new LlanowarElves();
        harness.setHand(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with an empty hand finishes normally")
    void acceptingWithEmptyHandDoesNothing() {
        addReadyScout(player1);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Putting one land does not consume the normal land play")
    void putsOnlyOneLandAndPreservesNormalLandPlay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addReadyScout(player1);
        Forest chosen = new Forest();
        Forest remaining = new Forest();
        harness.setHand(player1, List.of(chosen, remaining));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.playLand(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    private Permanent addReadyScout(Player player) {
        Permanent scout = harness.addToBattlefieldAndReturn(player, new LlanowarScout());
        scout.setSummoningSick(false);
        return scout;
    }
}
