package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MastersGuidance.class, Forest.class, GrizzlyBears.class,
        IsamaruHoundOfKonda.class, KondaLordOfEiganjo.class})
class MastersGuidanceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on up to two target attacking creatures after attacking with two legendary creatures")
    void countersTwoTargetAttackingCreatures() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(isamaru),
                gd.playerBattlefields.get(player1.getId()).indexOf(konda),
                gd.playerBattlefields.get(player1.getId()).indexOf(bears)));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(isamaru.getId(), konda.getId(), bears.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(isamaru.getId(), konda.getId()));
        harness.passBothPriorities();

        assertThat(isamaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(konda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when fewer than two legendary creatures attack")
    void doesNotTriggerWithOnlyOneLegendaryAttacker() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(isamaru),
                gd.playerBattlefields.get(player1.getId()).indexOf(bears)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(isamaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Draws a card at the end step when you control a creature with power four or greater")
    void drawsAtEndStepWithHighPowerCreature() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw at the end step without a creature with power four or greater")
    void doesNotDrawAtEndStepWithLowPowerCreatures() {
        harness.addToBattlefield(player1, new MastersGuidance());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May choose no attacking creatures for counters")
    void mayChooseNoTargets() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());

        declareAttackers(List.of(1, 2));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(isamaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(konda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("May choose just one nonlegendary attacker, but cannot choose a nonattacking creature")
    void mayChooseOneNonlegendaryAttacker() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(isamaru.getId(), konda.getId(), bears.getId());
        assertThat(choice.validIds()).doesNotContain(nonattacker.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(isamaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(konda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The attack trigger still resolves after a legendary attacker leaves the battlefield")
    void losingLegendaryAttackerDoesNotUndoAttackEvent() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        harness.handleMultiplePermanentsChosen(player1, List.of(isamaru.getId(), bears.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(konda);
        gd.playerGraveyards.get(player1.getId()).add(konda.getCard());
        harness.passBothPriorities();

        assertThat(isamaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Does not draw if the qualifying creature's power falls below four before resolution")
    void rechecksPowerWhenEndStepTriggerResolves() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        bears.setPowerModifier(0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Still puts a counter on the remaining legal target when the other stops attacking")
    void resolvesForRemainingLegalTarget() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());

        declareAttackers(List.of(1, 2));
        harness.handleMultiplePermanentsChosen(player1, List.of(isamaru.getId(), konda.getId()));
        isamaru.setAttacking(false);
        harness.passBothPriorities();

        assertThat(isamaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(konda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Legendary creatures that stay back do not count toward the attack condition")
    void nonattackingLegendaryCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent isamaru = addCreatureReady(player1, new IsamaruHoundOfKonda());
        addCreatureReady(player1, new KondaLordOfEiganjo());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 3));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(isamaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's powerful creature does not satisfy the end-step condition")
    void opponentsCreatureDoesNotQualifyForDraw() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new MastersGuidance());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
