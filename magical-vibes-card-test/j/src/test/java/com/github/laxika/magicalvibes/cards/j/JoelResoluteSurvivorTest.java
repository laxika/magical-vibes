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

    @Test
    @DisplayName("A nontoken death does not consume the trigger for an opponent's token")
    void nontokenDeathDoesNotConsumeOpponentTokenTrigger() {
        Permanent joel = harness.addToBattlefieldAndReturn(player1, new JoelResoluteSurvivor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent token = addTokenCreature(player2);
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        kill(creature);
        kill(token);

        assertThat(joel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
    }

    @Test
    @DisplayName("The turn limit applies before the first trigger resolves")
    void pendingTriggerStillConsumesTurnLimit() {
        Permanent joel = harness.addToBattlefieldAndReturn(player1, new JoelResoluteSurvivor());
        Permanent firstToken = addTokenCreature(player1);
        Permanent secondToken = addTokenCreature(player2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        firstToken.setMarkedDamage(firstToken.getEffectiveToughness());
        harness.runStateBasedActions();
        secondToken.setMarkedDamage(secondToken.getEffectiveToughness());
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(joel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Joel draws once when he and multiple tokens die simultaneously")
    void simultaneousDeathStillDrawsOnce() {
        Permanent joel = harness.addToBattlefieldAndReturn(player1, new JoelResoluteSurvivor());
        Permanent firstToken = addTokenCreature(player1);
        Permanent secondToken = addTokenCreature(player2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        joel.setMarkedDamage(joel.getEffectiveToughness());
        firstToken.setMarkedDamage(firstToken.getEffectiveToughness());
        secondToken.setMarkedDamage(secondToken.getEffectiveToughness());
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Joel, Resolute Survivor");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Joel can trigger again during the next player's turn")
    void triggerLimitResetsOnOpponentsTurn() {
        Permanent joel = harness.addToBattlefieldAndReturn(player1, new JoelResoluteSurvivor());
        Permanent firstToken = addTokenCreature(player1);
        Permanent secondToken = addTokenCreature(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        kill(firstToken);
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);
        kill(secondToken);

        assertThat(joel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
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
