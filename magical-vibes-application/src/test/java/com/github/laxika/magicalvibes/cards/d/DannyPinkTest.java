package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GavonyTownship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DannyPink.class, Forest.class, GavonyTownship.class, GrizzlyBears.class})
class DannyPinkTest extends BaseCardTest {

    @BeforeEach
    void clearInitialHand() {
        harness.setHand(player1, List.of());
    }

    @Test
    @DisplayName("Mentor puts a +1/+1 counter on a lesser-power attacking creature")
    void mentorCountersLesserPowerAttacker() {
        Permanent danny = addCreatureReady(player1, new DannyPink());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(danny, attacker);

        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each creature draws only once when counters are first put on it each turn")
    void creaturesDrawOncePerTurnWhenCountersArePutOnThem() {
        Permanent danny = addCreatureReady(player1, new DannyPink());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        activateTownship(township);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(danny.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        township.untap();
        activateTownship(township);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    private void activateTownship(Permanent township) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(township), 1, null, null);
        harness.passBothPriorities();
    }

    private void declareAttackers(Permanent first, Permanent second) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
    }
}
