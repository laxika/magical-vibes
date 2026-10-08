package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AuspiciousArrival;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VojaJawsOfTheConclave.class, HowlpackWolf.class, GrizzlyBears.class, LlanowarElves.class,
        AuspiciousArrival.class})
class VojaJawsOfTheConclaveTest extends BaseCardTest {

    @Test
    void attackingPutsElfCountCountersOnEachCreatureAndDrawsForEachWolf() {
        Permanent voja = addCreatureReady(player1, new VojaJawsOfTheConclave());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new LlanowarElves());
        Permanent wolf = addCreatureReady(player1, new HowlpackWolf());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentElf = addCreatureReady(player2, new LlanowarElves());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(voja.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentElf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void doesNotTriggerWhenAnotherCreatureAttacks() {
        Permanent voja = addCreatureReady(player1, new VojaJawsOfTheConclave());
        addCreatureReady(player1, new LlanowarElves());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(voja.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void drawsForVojaEvenWhenNoElvesAreControlled() {
        Permanent voja = addCreatureReady(player1, new VojaJawsOfTheConclave());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(voja.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(voja.isTapped()).isFalse();
    }

    @Test
    void countsElvesAndWolvesAtResolutionAfterVojaLeaves() {
        Permanent voja = addCreatureReady(player1, new VojaJawsOfTheConclave());
        Permanent departedElf = addCreatureReady(player1, new LlanowarElves());
        Permanent remainingElf = addCreatureReady(player1, new LlanowarElves());
        Permanent wolf = addCreatureReady(player1, new HowlpackWolf());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(voja);
        gd.playerBattlefields.get(player1.getId()).remove(departedElf);
        Permanent arrivingCreature = addCreatureReady(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(remainingElf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(arrivingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void drawsNothingWhenNoWolvesRemainAtResolution() {
        Permanent voja = addCreatureReady(player1, new VojaJawsOfTheConclave());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(voja);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void wardCountersOpponentsSpellWhenPaymentIsDeclined() {
        Permanent voja = harness.addToBattlefieldAndReturn(player1, new VojaJawsOfTheConclave());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AuspiciousArrival()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, voja.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Auspicious Arrival");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(voja);
    }
}
