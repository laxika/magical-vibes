package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoelResoluteSurvivor.class, Forest.class, GrizzlyBears.class})
class JoelResoluteSurvivorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on Joel and draws when a creature token dies")
    void tokenCreatureDeathPutsCounterAndDraws() {
        Permanent joel = harness.addToBattlefieldAndReturn(player1, new JoelResoluteSurvivor());
        Permanent token = addTokenCreature(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        kill(token);

        assertThat(joel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not trigger when a nontoken creature dies")
    void nontokenCreatureDeathDoesNotTrigger() {
        Permanent joel = harness.addToBattlefieldAndReturn(player1, new JoelResoluteSurvivor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        kill(creature);

        assertThat(joel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Triggers only once when multiple creature tokens die in one turn")
    void triggersOnlyOncePerTurn() {
        Permanent joel = harness.addToBattlefieldAndReturn(player1, new JoelResoluteSurvivor());
        Permanent firstToken = addTokenCreature(player1);
        Permanent secondToken = addTokenCreature(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        kill(firstToken);
        kill(secondToken);

        assertThat(joel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private Permanent addTokenCreature(com.github.laxika.magicalvibes.model.Player player) {
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        return harness.addToBattlefieldAndReturn(player, tokenCard);
    }

    private void kill(Permanent creature) {
        creature.setMarkedDamage(creature.getEffectiveToughness());
        harness.runStateBasedActions();
        resolveAllTriggers();
    }
}
