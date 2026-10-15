package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DacksDuplicate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.Reanimate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarchesaTheBlackRose.class, GrizzlyBears.class, LightningBolt.class,
        DacksDuplicate.class, Reanimate.class})
class MarchesaTheBlackRoseTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have dethrone")
    void grantsDethroneToOtherCreaturesYouControl() {
        addMarchesa();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DETHRONE)).isTrue();
    }

    @Test
    @DisplayName("A countered creature you control returns at the next end step")
    void returnsCounteredCreatureAtNextEndStep() {
        addMarchesa();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyBears();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature without a +1/+1 counter does not return")
    void doesNotReturnUncounteredCreature() {
        addMarchesa();
        addCreatureReady(player1, new GrizzlyBears());

        destroyBears();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Marchesa returns herself if she dies with a +1/+1 counter")
    void returnsItselfWithCounter() {
        Permanent marchesa = addCreatureReady(player1, new MarchesaTheBlackRose());
        marchesa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        marchesa.setMarkedDamage(4);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Marchesa, the Black Rose");
        harness.assertNotInGraveyard(player1, "Marchesa, the Black Rose");
    }

    @Test
    @DisplayName("Marchesa and an ally both get dethrone counters when attacking a tied highest-life player")
    void ownAndGrantedDethroneTrigger() {
        Permanent marchesa = addCreatureReady(player1, new MarchesaTheBlackRose());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(marchesa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dethrone does not trigger when the attacker has the highest life total")
    void dethroneDoesNotTriggerAgainstLowerLife() {
        Permanent marchesa = addCreatureReady(player1, new MarchesaTheBlackRose());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 21);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(marchesa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opposing creatures neither gain dethrone nor return through Marchesa")
    void opponentCreatureIsNotAffected() {
        addMarchesa();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DETHRONE)).isFalse();
        bears.setMarkedDamage(3);

        harness.runStateBasedActions();
        advanceToEndStep();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Marchesa does not return herself without a +1/+1 counter")
    void doesNotReturnItselfWithoutCounter() {
        Permanent marchesa = addCreatureReady(player1, new MarchesaTheBlackRose());
        marchesa.setMarkedDamage(3);

        harness.runStateBasedActions();
        advanceToEndStep();

        harness.assertInGraveyard(player1, "Marchesa, the Black Rose");
        harness.assertNotOnBattlefield(player1, "Marchesa, the Black Rose");
    }

    @Test
    @DisplayName("Marchesa sees a countered ally die simultaneously with her")
    void simultaneousDeathsReturnBothCreatures() {
        Permanent marchesa = addCreatureReady(player1, new MarchesaTheBlackRose());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        marchesa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        marchesa.setMarkedDamage(4);
        bears.setMarkedDamage(3);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Marchesa, the Black Rose");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The scheduled return works even after Marchesa leaves the battlefield")
    void scheduledReturnSurvivesSourceDeath() {
        Permanent marchesa = addCreatureReady(player1, new MarchesaTheBlackRose());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        destroyBears();
        marchesa.setMarkedDamage(3);
        harness.runStateBasedActions();

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Marchesa, the Black Rose");
    }

    @Test
    @DisplayName("The delayed return uses the stack at the next end step")
    void endStepReturnAllowsResponses() {
        addMarchesa();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        destroyBears();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A card that leaves and re-enters the graveyard is not returned by the old delayed trigger")
    void oldDelayedReturnDoesNotFollowNewGraveyardObject() {
        addMarchesa();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        destroyBears();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Reanimate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, bears.getCard().getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        returnedBears.setMarkedDamage(2);
        harness.runStateBasedActions();

        advanceToEndStep();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Granted dethrone triggers separately from Dack's Duplicate's own dethrone")
    void grantedDethroneAddsAnotherTrigger() {
        addMarchesa();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.castFromHand(player1, new DacksDuplicate(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent duplicate = findPermanent(player1, "Grizzly Bears");
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(duplicate)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(duplicate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature owned by the opponent returns under Marchesa's controller")
    void returnsOpponentOwnedCreatureUnderYourControl() {
        addMarchesa();
        GrizzlyBears bearsCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bearsCard));
        harness.setHand(player1, List.of(new Reanimate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, bearsCard.getId());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyBears();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Death during an end step waits for the following turn's end step")
    void deathDuringEndStepWaitsForNextEndStep() {
        addMarchesa();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        bears.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.forceActivePlayer(player2);
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void addMarchesa() {
        addCreatureReady(player1, new MarchesaTheBlackRose());
    }

    private void destroyBears() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
