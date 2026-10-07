package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({SwoopingProtector.class, Shock.class, Murder.class})
class SwoopingProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows casting during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new SwoopingProtector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.passPriority(gd, player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent protector = castProtector();

        assertThat(protector.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its shield counter prevents one damage event")
    void shieldCounterPreventsDamage() {
        Permanent protector = castProtector();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, protector.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protector);
        assertThat(protector.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(protector.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Entering without being cast still supplies a shield counter immediately")
    void entersWithoutCastingWithShieldCounter() {
        Permanent protector = harness.enterBattlefieldAndReturn(player1, new SwoopingProtector());

        assertThat(protector.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Swooping Protector");
    }

    @Test
    @DisplayName("A second damage event kills it after its shield counter is consumed")
    void secondDamageEventKillsProtector() {
        Permanent protector = castProtector();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, protector.getId());

        harness.assertOnBattlefield(player1, "Swooping Protector");
        assertThat(protector.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(protector.getMarkedDamage()).isZero();

        harness.castAndResolveInstant(player1, 0, protector.getId());

        harness.assertNotOnBattlefield(player1, "Swooping Protector");
        harness.assertInGraveyard(player1, "Swooping Protector");
    }

    @Test
    @DisplayName("A shield counter replaces one destruction but not a second")
    void shieldCounterReplacesOnlyFirstDestruction() {
        Permanent protector = castProtector();

        harness.setHand(player2, List.of(new Murder(), new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, protector.getId());

        harness.assertOnBattlefield(player1, "Swooping Protector");
        harness.assertNotInGraveyard(player1, "Swooping Protector");
        assertThat(protector.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(protector.isTapped()).isFalse();

        harness.castAndResolveInstant(player2, 0, protector.getId());

        harness.assertNotOnBattlefield(player1, "Swooping Protector");
        harness.assertInGraveyard(player1, "Swooping Protector");
    }

    private Permanent castProtector() {
        harness.castFromHand(player1, new SwoopingProtector(), "{3}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Swooping Protector");
    }
}
