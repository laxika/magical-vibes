package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FlyingOctobot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentVenom.class, Forest.class, FlyingOctobot.class, Shock.class})
class AgentVenomTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature you control dying draws a card and costs 1 life")
    void nontokenAllyDeathDrawsAndLosesLife() {
        addCreatureReady(player1, new AgentVenom());
        addCreatureReady(player1, new FlyingOctobot());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        killWithShock(player1, "Flying Octobot");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A token creature dying does not trigger Agent Venom")
    void tokenAllyDeathDoesNotTrigger() {
        addCreatureReady(player1, new AgentVenom());
        Card token = new FlyingOctobot();
        token.setToken(true);
        addCreatureReady(player1, token);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        killWithShock(player1, "Flying Octobot");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Agent Venom")
    void opponentCreatureDeathDoesNotTrigger() {
        addCreatureReady(player1, new AgentVenom());
        addCreatureReady(player2, new FlyingOctobot());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        killWithShock(player2, "Flying Octobot");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Agent Venom dying does not trigger its own ability")
    void selfDeathDoesNotTrigger() {
        addCreatureReady(player1, new AgentVenom());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = gd.getLife(player1.getId());
        UUID agentId = harness.getPermanentId(player1, "Agent Venom");
        harness.castAndResolveInstant(player1, 0, agentId);
        harness.castAndResolveInstant(player1, 0, agentId);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Agent Venom");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A pending death trigger resolves after Agent Venom dies")
    void pendingTriggerResolvesAfterSourceDies() {
        addCreatureReady(player1, new AgentVenom());
        addCreatureReady(player1, new FlyingOctobot());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Flying Octobot"));
        assertThat(gd.stack).hasSize(1);
        UUID agentId = harness.getPermanentId(player1, "Agent Venom");
        harness.castAndResolveInstant(player1, 0, agentId);
        harness.castAndResolveInstant(player1, 0, agentId);
        harness.assertInGraveyard(player1, "Agent Venom");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    private void killWithShock(Player creatureController, String targetName) {
        UUID targetId = harness.getPermanentId(creatureController, targetName);
        harness.castAndResolveInstant(player1, 0, targetId);
        resolveAllTriggers();
    }
}
