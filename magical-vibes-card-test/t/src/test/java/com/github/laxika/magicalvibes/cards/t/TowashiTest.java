package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ElspethSunsChampion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Towashi.class, GrizzlyBears.class, Forest.class, ElspethSunsChampion.class,
        LeoninScimitar.class, Pacifism.class})
class TowashiTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Towashi(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void modifiedCreaturesGainTrampleAndDrawOnCombatDamage() {
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unmodified = addCreatureReady(player1, new GrizzlyBears());
        modified.setAttacking(true);
        unmodified.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gqs.hasKeyword(gd, modified, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodified, Keyword.TRAMPLE)).isFalse();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void chaosDistributesThreeCountersAmongTargetCreaturesYouControl() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextETBTokenMultiTargetTrigger(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "3");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void onlyAurasControlledByCreatureControllerCountAsModifications() {
        Permanent ownEnchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentEnchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        ownAura.setAttachedTo(ownEnchanted.getId());
        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        opponentAura.setAttachedTo(opponentEnchanted.getId());

        assertThat(gqs.hasKeyword(gd, ownEnchanted, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentEnchanted, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void equipmentControlledByOpponentStillModifiesCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void anyCounterModifiesCreatureAndRemovingLastCounterRemovesAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        creature.setCounterCount(CounterType.CHARGE, 0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        creature.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void modifiedOpponentCreaturesDoNotGainAbilities() {
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.CHARGE, 1);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void abilitiesFollowPlanarController() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.CHARGE, 1);
        second.setCounterCount(CounterType.CHARGE, 1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isFalse();

        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();

        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void eachModifiedCreatureDrawsOneCardRegardlessOfDamageAmount() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        first.setAttacking(true);
        second.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void combatDamageToPlaneswalkerDrawsOneCard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethSunsChampion());
        creature.setAttacking(true);
        creature.setAttackTarget(planeswalker.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void chaosCanDistributeOneCounterToEachOfThreeCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextETBTokenMultiTargetTrigger(gd));
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "1");
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void chaosDoesNotRedistributeCountersFromTargetThatLeavesBattlefield() {
        Permanent survivor = addCreatureReady(player1, new GrizzlyBears());
        Permanent removed = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextETBTokenMultiTargetTrigger(gd));
        harness.handlePermanentChosen(player1, survivor.getId());
        harness.handlePermanentChosen(player1, removed.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "2");
        gd.playerBattlefields.get(player1.getId()).remove(removed);
        gd.playerGraveyards.get(player1.getId()).add(removed.getCard());
        resolveAllTriggers();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(removed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
