package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KrovikanVampireTest extends BaseCardTest {

    /** Vampire blocks/attacks and kills a Grizzly Bears in combat; leaves the game mid-turn. */
    private void vampireKillsBearsInCombat() {
        harness.addToBattlefield(player1, new KrovikanVampire());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).getFirst();
        vampire.setSummoningSick(false);
        vampire.setAttacking(true);

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        bears.setSummoningSick(false);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Combat damage kills the Bears; do not advance through end step yet.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStepAndResolve() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature the Vampire kills returns under its controller at end step")
    void returnsDamagedCreatureUnderControlAtEndStep() {
        vampireKillsBearsInCombat();
        advanceToEndStepAndResolve();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature the Vampire did not damage does not return when it dies")
    void noReturnForUndamagedCreature() {
        harness.addToBattlefield(player1, new KrovikanVampire());
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

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Losing control of the Vampire makes its controller sacrifice the returned creature")
    void sacrificesReturnedCreatureOnControlLoss() {
        vampireKillsBearsInCombat();
        advanceToEndStepAndResolve();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Krovikan Vampire"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(vampire);
        gd.playerBattlefields.get(player2.getId()).add(vampire);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("If the Vampire leaves before end step, nothing is returned")
    void noReturnIfVampireLeavesBeforeEndStep() {
        harness.addToBattlefield(player1, new KrovikanVampire());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        // Record that Vampire damaged Bears this turn (non-combat), then destroy Bears.
        gd.creatureCardsDamagedThisTurnBySourcePermanent
                .computeIfAbsent(vampire.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(bears.getCard().getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.sourcesWhoseDamagedCreaturesDiedThisTurn).contains(vampire.getId());

        // Vampire leaves before end step — ability must not trigger.
        gd.playerBattlefields.get(player1.getId()).remove(vampire);
        gd.playerGraveyards.get(player1.getId()).add(vampire.getCard());
        gd.stack.clear();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}
