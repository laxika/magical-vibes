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

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(isamaru.getId(), konda.getId(), bears.getId());

        harness.handlePermanentChosen(player1, isamaru.getId());
        harness.handlePermanentChosen(player1, konda.getId());
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

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
