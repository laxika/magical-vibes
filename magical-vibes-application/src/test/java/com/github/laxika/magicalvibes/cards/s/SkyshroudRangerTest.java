package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
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

@CardUsed({SkyshroudRanger.class, Forest.class, MetallicSliver.class})
class SkyshroudRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Skyshroud Ranger and puts ability on stack")
    void activatingTapsAndUsesStack() {
        Permanent ranger = addReadyRanger(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ranger.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(SkyshroudRanger.class);
    }

    @Test
    @DisplayName("Resolving ability prompts may choice first")
    void resolvingPromptsMayChoice() {
        addReadyRanger(player1);
        harness.setHand(player1, List.of(new Forest(), new MetallicSliver()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may prompt allows choosing only land cards from hand")
    void acceptingMayPromptsLandChoice() {
        addReadyRanger(player1);
        harness.setHand(player1, List.of(new MetallicSliver(), new Forest(), new MetallicSliver()));

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
        addReadyRanger(player1);
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && !permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ability can put a land onto battlefield even if a land was already played this turn")
    void canPutLandEvenAfterLandPlay() {
        addReadyRanger(player1);
        harness.setHand(player1, List.of(new Forest()));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest);
    }

    @Test
    @DisplayName("Accepting may choice with no land in hand does nothing")
    void acceptingMayWithNoLandDoesNothing() {
        addReadyRanger(player1);
        harness.setHand(player1, List.of(new MetallicSliver()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining may choice leaves hand unchanged")
    void decliningMayLeavesHandUnchanged() {
        addReadyRanger(player1);
        harness.setHand(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Cannot activate while Skyshroud Ranger has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new SkyshroudRanger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate while Skyshroud Ranger is tapped")
    void cannotActivateWhileTapped() {
        Permanent ranger = addReadyRanger(player1);
        ranger.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        addReadyRanger(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate outside main phase")
    void cannotActivateOutsideMainPhase() {
        addReadyRanger(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate when stack is not empty")
    void cannotActivateWhenStackNotEmpty() {
        addReadyRanger(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.stack.add(new StackEntry(
                StackEntryType.ACTIVATED_ABILITY,
                new MetallicSliver(),
                player2.getId(),
                "dummy ability",
                List.of()
        ));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    private Permanent addReadyRanger(Player player) {
        return addCreatureReady(player, new SkyshroudRanger());
    }
}
