package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GatewaySneak.class, Forest.class, RakdosGuildgate.class})
class GatewaySneakTest extends BaseCardTest {

    @Test
    @DisplayName("A Gate entering under its controller's control makes Gateway Sneak unblockable")
    void gateEnteringMakesSneakUnblockable() {
        Permanent sneak = addSneakReady();
        harness.setHand(player1, List.of(new RakdosGuildgate()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(sneak.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A non-Gate land entering does not make Gateway Sneak unblockable")
    void nonGateEnteringDoesNotMakeSneakUnblockable() {
        Permanent sneak = addSneakReady();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(sneak.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Gateway Sneak draws a card when it deals combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        Permanent sneak = addSneakReady();
        harness.setLibrary(player1, List.of(new Forest()));
        sneak.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Gateway Sneak's unblockable effect wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        Permanent sneak = addSneakReady();
        harness.setHand(player1, List.of(new RakdosGuildgate()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sneak.isCantBeBlocked()).isFalse();
    }

    private Permanent addSneakReady() {
        Permanent sneak = harness.addToBattlefieldAndReturn(player1, new GatewaySneak());
        sneak.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return sneak;
    }

    @Test
    @DisplayName("An opponent's Gate does not make Gateway Sneak unblockable")
    void opponentsGateDoesNotTrigger() {
        Permanent sneak = addSneakReady();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RakdosGuildgate()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(sneak.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Each Gateway Sneak becomes unblockable only when its own Gate trigger resolves")
    void gateTriggersForEachSneak() {
        Permanent first = addSneakReady();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GatewaySneak());
        harness.setHand(player1, List.of(new RakdosGuildgate()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(first.isCantBeBlocked()).isFalse();
        assertThat(second.isCantBeBlocked()).isFalse();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Combat damage draws for Gateway Sneak's controller, not the damaged player")
    void opponentControlledSneakDrawsForOpponent() {
        Permanent sneak = harness.addToBattlefieldAndReturn(player2, new GatewaySneak());
        sneak.setSummoningSick(false);
        sneak.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Forest drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard));

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

}
