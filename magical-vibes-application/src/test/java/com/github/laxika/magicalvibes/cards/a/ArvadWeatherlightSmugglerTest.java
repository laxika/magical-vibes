package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArvadWeatherlightSmuggler.class, GrizzlyBears.class, Shock.class})
class ArvadWeatherlightSmugglerTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually gets +X/+X for all creatures that died this turn")
    void perpetuallyCountsCreaturesThatDiedThisTurn() {
        Permanent arvad = addCreatureReady(player1, new ArvadWeatherlightSmuggler());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player2, new GrizzlyBears());

        shock(firstBear.getId());
        shock(secondBear.getId());
        advanceToEndStep();

        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when no creature died this turn")
    void doesNotTriggerWithoutCreatureDeath() {
        Permanent arvad = addCreatureReady(player1, new ArvadWeatherlightSmuggler());

        advanceToEndStep();

        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers from the graveyard and applies when the card returns")
    void triggersFromGraveyard() {
        ArvadWeatherlightSmuggler arvad = new ArvadWeatherlightSmuggler();
        harness.setGraveyard(player1, List.of(arvad));
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        shock(bear.getId());
        advanceToEndStep();

        Permanent returnedArvad = harness.enterBattlefieldAndReturn(player1, arvad);
        assertThat(gqs.getEffectivePower(gd, returnedArvad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returnedArvad)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts additional deaths while the end-step trigger is on the stack")
    void countsDeathsAtResolution() {
        Permanent arvad = addCreatureReady(player1, new ArvadWeatherlightSmuggler());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player2, new GrizzlyBears());
        shock(firstBear.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        shock(secondBear.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(3);
    }

    @Test
    @DisplayName("The battlefield ability does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent arvad = addCreatureReady(player1, new ArvadWeatherlightSmuggler());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        shock(bear.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(1);
    }

    @Test
    @DisplayName("Arvad's own death enables its graveyard trigger")
    void countsItsOwnDeath() {
        ArvadWeatherlightSmuggler card = new ArvadWeatherlightSmuggler();
        Permanent arvad = addCreatureReady(player1, card);

        shock(arvad.getId());
        harness.assertInGraveyard(player1, "Arvad, Weatherlight Smuggler");
        advanceToEndStep();

        Permanent returnedArvad = harness.enterBattlefieldAndReturn(player1, card);
        assertThat(gqs.getEffectivePower(gd, returnedArvad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returnedArvad)).isEqualTo(2);
    }

    @Test
    @DisplayName("The graveyard ability does not trigger without a creature death")
    void graveyardAbilityRequiresCreatureDeath() {
        ArvadWeatherlightSmuggler card = new ArvadWeatherlightSmuggler();
        harness.setGraveyard(player1, List.of(card));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        Permanent returnedArvad = harness.enterBattlefieldAndReturn(player1, card);
        assertThat(gqs.getEffectivePower(gd, returnedArvad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returnedArvad)).isEqualTo(1);
    }

    @Test
    @DisplayName("The graveyard ability does not trigger during an opponent's end step")
    void graveyardAbilityDoesNotTriggerDuringOpponentsEndStep() {
        ArvadWeatherlightSmuggler card = new ArvadWeatherlightSmuggler();
        harness.setGraveyard(player1, List.of(card));
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        shock(bear.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        Permanent returnedArvad = harness.enterBattlefieldAndReturn(player1, card);
        assertThat(gqs.getEffectivePower(gd, returnedArvad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returnedArvad)).isEqualTo(1);
    }

    @Test
    @DisplayName("A perpetual boost survives Arvad dying and returning")
    void boostPersistsAfterDeath() {
        ArvadWeatherlightSmuggler card = new ArvadWeatherlightSmuggler();
        Permanent arvad = addCreatureReady(player1, card);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        shock(bear.getId());
        advanceToEndStep();

        shock(arvad.getId());
        harness.assertInGraveyard(player1, "Arvad, Weatherlight Smuggler");
        Permanent returnedArvad = harness.enterBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, returnedArvad)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returnedArvad)).isEqualTo(2);
    }

    private void shock(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
