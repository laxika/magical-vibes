package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BloomwielderDryads;
import com.github.laxika.magicalvibes.cards.c.ChompingKavu;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloomwielderDryads.class, ChompingKavu.class, InvasionOfMoag.class, VolcanicSpite.class})
class InvasionOfMoagTest extends BaseCardTest {

    @Test
    void entersPutsCounterOnEachCreatureYouControl() {
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new ChompingKavu());
        Permanent secondFriendly = harness.addToBattlefieldAndReturn(player1, new ChompingKavu());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ChompingKavu());
        harness.setHand(player1, List.of(new InvasionOfMoag()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        resolveAllTriggers();

        assertThat(friendly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondFriendly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Invasion of Moag").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void defeatingTheSiegeCastsBloomwielderDryadsAndItsEndStepTriggerAddsACounter() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfMoag());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent dryads = findPermanent(player1, "Bloomwielder Dryads");
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ChompingKavu());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(dryads.isTransformed()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void controllerCanDeclineCastingTheDefeatedSiege() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfMoag());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Bloomwielder Dryads");
    }

    @Test
    void dryadsCanTargetThemselvesAtTheirControllersEndStep() {
        Permanent dryads = harness.addToBattlefieldAndReturn(player1, new BloomwielderDryads());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, dryads.getId());
        resolveAllTriggers();

        assertThat(dryads.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void dryadsDoNotTriggerAtOpponentsEndStep() {
        Permanent dryads = harness.addToBattlefieldAndReturn(player1, new BloomwielderDryads());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(dryads.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPay() {
        Permanent dryads = harness.addToBattlefieldAndReturn(player1, new BloomwielderDryads());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VolcanicSpite()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, dryads.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Volcanic Spite");
        harness.assertOnBattlefield(player1, "Bloomwielder Dryads");
        assertThat(dryads.getMarkedDamage()).isZero();
    }

    @Test
    void payingTwoManaForWardAllowsTheOpponentsSpellToResolve() {
        Permanent dryads = harness.addToBattlefieldAndReturn(player1, new BloomwielderDryads());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VolcanicSpite()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, dryads.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Volcanic Spite");
        harness.assertInGraveyard(player1, "Bloomwielder Dryads");
        harness.assertNotOnBattlefield(player1, "Bloomwielder Dryads");
    }
}
