package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        setDeck(player1, List.of(new Forest()));
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

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
