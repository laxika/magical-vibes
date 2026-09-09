package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AetherVial;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GavonyTownship;
import com.github.laxika.magicalvibes.cards.h.HopefulInitiate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloakedCadet.class, AirElemental.class, GavonyTownship.class, Forest.class,
        HopefulInitiate.class, Coretapper.class, AetherVial.class})
class CloakedCadetTest extends BaseCardTest {

    @Test
    void trainingPutsACounterOnCloakedCadet() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent airElemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(cadet),
                gd.playerBattlefields.get(player1.getId()).indexOf(airElemental)));
        harness.passBothPriorities();

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void drawsOnlyOnceWhenControlledHumanGetsCounters() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent firstTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent secondTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        activateTownship(player1, firstTownship);
        activateTownship(player1, secondTownship);

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    void doesNotTriggerForHumanControlledByOpponent() {
        addCreatureReady(player1, new CloakedCadet());
        Permanent township = harness.addToBattlefieldAndReturn(player2, new GavonyTownship());
        Permanent hopefulInitiate = addCreatureReady(player2, new HopefulInitiate());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        activateTownship(player2, township);

        assertThat(hopefulInitiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerForNonPlusOneCounters() {
        addCreatureReady(player1, new CloakedCadet());
        Permanent coretapper = addCreatureReady(player1, new Coretapper());
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(coretapper), 0, null, vial.getId());
        harness.passBothPriorities();

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void activateTownship(Player player, Permanent township) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.activateAbility(player,
                gd.playerBattlefields.get(player.getId()).indexOf(township), 1, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
