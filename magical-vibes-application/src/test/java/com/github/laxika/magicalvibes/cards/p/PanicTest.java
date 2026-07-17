package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PanicTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers: target can't block and a draw is scheduled")
    void targetCantBlockAndSchedulesDraw() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, blocker.getId());
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
    @DisplayName("Targeted creature actually cannot block")
    void targetedCreatureCannotBlock() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast once blockers are declared")
    void cannotCastDuringDeclareBlockers() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside combat")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.forceActivePlayer(player1);
        addReadyCreature(player2); // valid target so spell is playable
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Panic()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
