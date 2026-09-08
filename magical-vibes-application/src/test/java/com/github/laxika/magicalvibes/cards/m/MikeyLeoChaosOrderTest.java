package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GavonyTownship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MikeyLeoChaosOrder.class, Forest.class, FumeSpitter.class, GavonyTownship.class, GrizzlyBears.class})
class MikeyLeoChaosOrderTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void clearInitialHand() {
        harness.setHand(player1, List.of());
    }

    @Test
    @DisplayName("Draws a card when you put a counter on a creature you control")
    void drawsWhenCounterIsPutOnControlledCreature() {
        harness.addToBattlefield(player1, new MikeyLeoChaosOrder());
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        activateTownship(township);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new MikeyLeoChaosOrder());
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        activateTownship(township);
        resolveAllTriggers();
        township.untap();
        activateTownship(township);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent puts a counter on your creature")
    void doesNotTriggerForOpponentCounterPlacement() {
        harness.addToBattlefield(player1, new MikeyLeoChaosOrder());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FumeSpitter());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
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
}
