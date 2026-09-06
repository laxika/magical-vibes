package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage to a target creature")
    void preventsDamageToTargetCreature() {
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSwiftManeuver(defender.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevents the next 2 damage to a target player")
    void preventsDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        castSwiftManeuver(player2.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Draws a card at the beginning of the next turn's upkeep")
    void drawsAtNextUpkeep() {
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        castSwiftManeuver(player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    private void castSwiftManeuver(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SwiftManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
