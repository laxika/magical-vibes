package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TymorasInvoker.class})
class TymorasInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying eight mana with Tymora's Invoker draws two cards")
    void abilityDrawsTwoCards() {
        addInvoker();
        harness.setLibrary(player1, List.of(new TymorasInvoker(), new TymorasInvoker()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof TymorasInvoker)
                .hasSize(2);
    }

    @Test
    @DisplayName("Tymora's Invoker cannot activate without eight mana")
    void abilityRequiresEightMana() {
        addInvoker();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Invoker can activate on an opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new TymorasInvoker());
        invoker.setSummoningSick(true);
        invoker.tap();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TymorasInvoker(), new TymorasInvoker()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        assertThat(invoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Invoker can activate twice without tapping")
    void canActivateRepeatedly() {
        Permanent invoker = addInvoker();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TymorasInvoker(), new TymorasInvoker(),
                new TymorasInvoker(), new TymorasInvoker()));
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability still draws after the Invoker leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent invoker = addInvoker();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TymorasInvoker(), new TymorasInvoker()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(invoker);
        gd.playerGraveyards.get(player1.getId()).add(invoker.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private Permanent addInvoker() {
        return addCreatureReady(player1, new TymorasInvoker());
    }
}
