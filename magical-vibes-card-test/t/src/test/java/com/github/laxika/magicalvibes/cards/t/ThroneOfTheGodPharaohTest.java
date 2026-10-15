package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThroneOfTheGodPharaoh.class, Colossapede.class})
class ThroneOfTheGodPharaohTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses life equal to tapped creatures you control at your end step")
    void opponentLosesLifePerTappedCreature() {
        harness.addToBattlefield(player1, new ThroneOfTheGodPharaoh());

        Permanent tapped1 = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent tapped2 = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        tapped1.tap();
        tapped2.tap();
        // Untapped creature is not counted.
        harness.addToBattlefield(player1, new Colossapede());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Advance to end step (fires the trigger), then resolve it.
        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("No life loss when you control no tapped creatures")
    void noLifeLossWithoutTappedCreatures() {
        harness.addToBattlefield(player1, new ThroneOfTheGodPharaoh());
        // Untapped creature only.
        harness.addToBattlefield(player1, new Colossapede());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new ThroneOfTheGodPharaoh());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        tapped.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapped noncreatures and opponents' tapped creatures are not counted")
    void countsOnlyControllersTappedCreatures() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfTheGodPharaoh());
        throne.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        ownCreature.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        opposingCreature.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Triggers with zero tapped creatures and counts creatures tapped before resolution")
    void countsCreaturesTappedAfterTrigger() {
        harness.addToBattlefield(player1, new ThroneOfTheGodPharaoh());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        creature.tap();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Creatures untapped before resolution no longer contribute to life loss")
    void doesNotCountCreaturesUntappedBeforeResolution() {
        harness.addToBattlefield(player1, new ThroneOfTheGodPharaoh());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        creature.tap();
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);
        creature.untap();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The end step trigger resolves after Throne leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new ThroneOfTheGodPharaoh());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        creature.tap();
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(throne);
        gd.playerGraveyards.get(player1.getId()).add(throne.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
