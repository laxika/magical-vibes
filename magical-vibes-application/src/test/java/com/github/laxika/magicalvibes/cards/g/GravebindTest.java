package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GravebindTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving marks the target so it can't be regenerated and schedules a draw")
    void marksTargetAndSchedulesDraw() {
        Permanent skele = addRegeneratingSkeleton(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        GameData gd = harness.getGameData();

        harness.castInstant(player1, 0, skele.getId());
        harness.passBothPriorities();

        assertThat(skele.isCantRegenerateThisTurn()).isTrue();
        assertThat(skele.getRegenerationShield()).isEqualTo(1);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("A marked creature dies in combat despite its regeneration shield")
    void markedCreatureDiesInCombatDespiteShield() {
        Permanent skele = addRegeneratingSkeleton(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, skele.getId());
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);
        skele.setBlocking(true);
        skele.addBlockingTargetId(bears.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent skele = addRegeneratingSkeleton(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        GameData gd = harness.getGameData();

        harness.castInstant(player1, 0, skele.getId());
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        stepTriggerService.handleUpkeepTriggers(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("The can't-be-regenerated mark clears during end-of-turn cleanup")
    void markClearsAtEndOfTurn() {
        Permanent skele = addRegeneratingSkeleton(player2);
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, skele.getId());
        harness.passBothPriorities();
        assertThat(skele.isCantRegenerateThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skele.isCantRegenerateThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Gravebind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addRegeneratingSkeleton(Player player) {
        Permanent perm = new Permanent(new DrudgeSkeletons());
        perm.setSummoningSick(false);
        perm.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
