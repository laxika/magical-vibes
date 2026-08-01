package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrosstownCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player mills that many cards")
    void millsCardsEqualToCombatDamage() {
        addAttackingCourier(player1);
        setLibrary(player2, 5);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills as many cards as the boosted combat damage dealt")
    void millScalesWithDamage() {
        Permanent courier = addAttackingCourier(player1);
        courier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // deals 4
        setLibrary(player2, 6);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("No mill when the Courier is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingCourier(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        setLibrary(player2, 5);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    private Permanent addAttackingCourier(Player player) {
        Permanent courier = addCreatureReady(player, new CrosstownCourier());
        courier.setAttacking(true);
        return courier;
    }

    private void setLibrary(Player player, int size) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Forest());
        }
        harness.setLibrary(player, cards);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        // Resolve the mill trigger without advancing the turn (opponent would draw).
        resolveAllTriggers();
    }
}
