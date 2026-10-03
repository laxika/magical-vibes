package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.q.QuicksilverBrashBlur;
import com.github.laxika.magicalvibes.cards.u.UltronDrone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdvancingTheSpirit.class, QuicksilverBrashBlur.class, AerialDoombot.class,
        UltronDrone.class})
class AdvancingTheSpiritTest extends BaseCardTest {

    @Test
    void drawsACardWhenItEnters() {
        Card drawnCard = new QuicksilverBrashBlur();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player1, new AdvancingTheSpirit());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void makesTheFirstPowerUpActivationFreeAndOnlyThatActivation() {
        harness.addToBattlefield(player1, new AdvancingTheSpirit());
        Permanent firstQuicksilver = addCreatureReady(player1, new QuicksilverBrashBlur());
        Permanent secondCreature = addCreatureReady(player1, new AerialDoombot());
        Permanent thirdCreature = addCreatureReady(player1, new UltronDrone());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstQuicksilver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new QuicksilverBrashBlur()));
        harness.setLibrary(player2, List.of(new QuicksilverBrashBlur()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 3, null, null);
        harness.passBothPriorities();

        assertThat(thirdCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotMakePowerUpFreeDuringAnOpponentsTurn() {
        harness.addToBattlefield(player1, new AdvancingTheSpirit());
        Permanent creature = addCreatureReady(player1, new AerialDoombot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void countsPowerUpActivatedBeforeTheEnchantmentEntered() {
        Permanent firstCreature = addCreatureReady(player1, new AerialDoombot());
        Permanent secondCreature = addCreatureReady(player1, new UltronDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new QuicksilverBrashBlur()));
        harness.enterBattlefieldAndReturn(player1, new AdvancingTheSpirit());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void multipleCopiesDoNotMakeTheSecondPowerUpFree() {
        harness.addToBattlefield(player1, new AdvancingTheSpirit());
        harness.addToBattlefield(player1, new AdvancingTheSpirit());
        Permanent firstCreature = addCreatureReady(player1, new AerialDoombot());
        Permanent secondCreature = addCreatureReady(player1, new UltronDrone());

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 3, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 3, null, null);
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
