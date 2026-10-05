package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuluSternGuardian.class, GrizzlyBears.class, KarnLiberated.class})
class LuluSternGuardianTest extends BaseCardTest {

    @Test
    void putsAStunCounterOnOneAttackingCreature() {
        addCreatureReady(player1, new LuluSternGuardian());
        Permanent firstAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());

        harness.handlePermanentChosen(player1, firstAttacker.getId());
        harness.passBothPriorities();

        assertThat(firstAttacker.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(secondAttacker.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void proliferatesWithActivatedAbility() {
        addCreatureReady(player1, new LuluSternGuardian());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotTargetACreatureAttackingYourPlaneswalker() {
        addCreatureReady(player1, new LuluSternGuardian());
        Permanent karn = harness.addToBattlefieldAndReturn(player1, new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 6);
        Permanent attackingYou = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0, 1),
                Map.of(0, player1.getId(), 1, karn.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attackingYou.getId());
        harness.handlePermanentChosen(player1, attackingYou.getId());
        harness.passBothPriorities();

        assertThat(attackingYou.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void proliferatesEveryCounterKindOnChosenPermanentsAndPlayers() {
        Permanent lulu = addCreatureReady(player1, new LuluSternGuardian());
        Permanent chosen = addCreatureReady(player2, new LuluSternGuardian());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        chosen.setCounterCount(CounterType.STUN, 1);
        lulu.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(lulu.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(lulu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void mayProliferateNothingEvenWhileTappedAndSummoningSick() {
        Permanent lulu = harness.addToBattlefieldAndReturn(player1, new LuluSternGuardian());
        lulu.setSummoningSick(true);
        lulu.tap();
        lulu.setCounterCount(CounterType.STUN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(lulu.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferateResolvesWhenNoCountersExist() {
        addCreatureReady(player1, new LuluSternGuardian());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerWhenItsControllerAttacks() {
        Permanent lulu = addCreatureReady(player1, new LuluSternGuardian());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(lulu.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void doesNotPutAStunCounterOnATargetThatStopsAttacking() {
        addCreatureReady(player1, new LuluSternGuardian());
        Permanent attacker = addCreatureReady(player2, new LuluSternGuardian());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.STUN)).isZero();
    }
}
