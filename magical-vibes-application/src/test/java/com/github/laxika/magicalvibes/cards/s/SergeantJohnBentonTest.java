package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SergeantJohnBenton.class, Forest.class, GrizzlyBears.class})
class SergeantJohnBentonTest extends BaseCardTest {

    @Test
    @DisplayName("Both players draw cards equal to the combat damage dealt")
    void bothPlayersDrawEqualToCombatDamage() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));
        harness.setLife(player2, 20);

        Permanent benton = addCreatureReady(player1, new SergeantJohnBenton());
        benton.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        benton.setAttacking(true);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger Share Intelligence")
    void blockedDamageDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Forest())));
        harness.setLife(player2, 20);

        Permanent benton = addCreatureReady(player1, new SergeantJohnBenton());
        benton.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
