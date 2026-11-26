package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DisruptDecorum;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hobble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaimaTheFracturedCalm.class, GrizzlyBears.class, Hobble.class, DisruptDecorum.class})
class KaimaTheFracturedCalmTest extends BaseCardTest {

    @Test
    @DisplayName("Goads each opponent's creature enchanted by your Aura and gets a counter for each")
    void goadsOpponentCreaturesEnchantedByYourAurasAndAddsCountersForEach() {
        Permanent kaima = addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent enchantedOpponent = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, enchantedOpponent);
        Permanent opponentWithOpponentsAura = addCreatureReady(player2, new GrizzlyBears());
        addAura(player2, opponentWithOpponentsAura);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, ownCreature);

        resolveControllerEndStep();

        assertThat(als.getMustAttackRequirementCount(gd, enchantedOpponent)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, opponentWithOpponentsAura)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, ownCreature)).isZero();
        assertThat(kaima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The goad ends as Kaima's controller's next turn begins")
    void goadExpiresAtKaimaControllersNextTurn() {
        addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent enchantedOpponent = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, enchantedOpponent);

        resolveControllerEndStep();
        gd.rulesEvents.occur(new TurnEvent.Began(player1.getId()));

        assertThat(als.getMustAttackRequirementCount(gd, enchantedOpponent)).isZero();
    }

    @Test
    @DisplayName("Counts each goaded creature once however many of your Auras enchant it")
    void countsEachCreatureOnceRegardlessOfNumberOfAuras() {
        Permanent kaima = addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent unenchanted = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, first);
        addAura(player1, first);
        addAura(player1, second);

        resolveControllerEndStep();

        assertThat(kaima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(als.getMustAttackRequirementCount(gd, first)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, second)).isEqualTo(1);
        assertThat(als.getMustAttackRequirementCount(gd, unenchanted)).isZero();
    }

    @Test
    @DisplayName("Removing the Aura after the ability resolves doesn't end the goad (ruling)")
    void removingAuraAfterResolutionDoesNotEndGoad() {
        addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = addAura(player1, creature);

        resolveControllerEndStep();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
        gd.rulesEvents.occur(new TurnEvent.Began(player2.getId()));
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Auras are checked on resolution, and the goad still happens if Kaima left")
    void checksAuraPresenceAtResolutionAndStillGoadsIfKaimaLeaves() {
        Permanent kaima = addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(kaima);
        addAura(player1, creature);
        resolveAllTriggers();

        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
        assertThat(kaima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature whose Aura left before resolution isn't goaded")
    void auraRemovedBeforeResolutionDoesNotQualify() {
        Permanent kaima = addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = addAura(player1, creature);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        resolveAllTriggers();

        assertThat(als.getMustAttackRequirementCount(gd, creature)).isZero();
        assertThat(kaima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Doesn't trigger at an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent kaima = addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, creature);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(als.getMustAttackRequirementCount(gd, creature)).isZero();
        assertThat(kaima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Goading an already goaded creature again adds no second attack requirement")
    void additionalEndStepDoesNotAddAnotherGoadRequirementForSamePlayer() {
        addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, creature);

        resolveControllerEndStep();
        resolveControllerEndStep();

        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature you already goaded is not goaded again this way and adds no counter (CR 701.15d)")
    void creatureAlreadyGoadedByYouIsNotCountedAsGoadedThisWay() {
        Permanent kaima = addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, creature);

        resolveControllerEndStep();
        resolveControllerEndStep();

        assertThat(kaima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature you goaded with another spell is not goaded this way and adds no counter (CR 701.15d)")
    void creatureGoadedByYourOtherSpellIsNotCountedAsGoadedThisWay() {
        Permanent kaima = addCreatureReady(player1, new KaimaTheFracturedCalm());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, creature);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, java.util.List.of(new DisruptDecorum()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        resolveControllerEndStep();

        assertThat(kaima.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isEqualTo(1);
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player controller, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new Hobble());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
