package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeraphTest extends BaseCardTest {

    /** Seraph blocks/attacks and kills a Grizzly Bears in combat, leaving the trigger resolved. */
    private void seraphKillsBearsInCombat() {
        harness.addToBattlefield(player1, new Seraph());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent seraph = gd.playerBattlefields.get(player1.getId()).getFirst();
        seraph.setSummoningSick(false);
        seraph.setAttacking(true);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        bears.setSummoningSick(false);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Combat damage kills the Bears; ON_DAMAGED_CREATURE_DIES trigger goes on the stack and resolves.
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature Seraph kills returns under Seraph's controller at the next end step")
    void returnsDamagedCreatureUnderControlAtEndStep() {
        seraphKillsBearsInCombat();

        // At the following end step the Bears returns under player1's control (Seraph's controller),
        // leaving player2's graveyard.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
    }

    @Test
    @DisplayName("A creature Seraph did not damage does not return when it dies")
    void noReturnForUndamagedCreature() {
        harness.addToBattlefield(player1, new Seraph());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();

        // It stays in the graveyard through the end step — no return.
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Losing control of Seraph makes its controller sacrifice the returned creature")
    void sacrificesReturnedCreatureOnControlLoss() {
        seraphKillsBearsInCombat();

        // The Bears has already returned under player1's control (Seraph's controller).
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Player2 gains control of Seraph (it stays on the battlefield, just under a new controller).
        Permanent seraph = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Seraph"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(seraph);
        gd.playerBattlefields.get(player2.getId()).add(seraph);

        // The next state-based-action check (here via resolving a spell) makes player1 sacrifice the
        // Bears; it dies to its owner's (player2's) graveyard.
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}
