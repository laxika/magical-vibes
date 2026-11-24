package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptWithBunnies.class, Forest.class})
class TemptWithBunniesTest extends BaseCardTest {

    @Test
    @DisplayName("An accepting opponent draws and creates a Rabbit, and so do you once more")
    void acceptingOpponentDrawsAndCreatesForBothPlayers() {
        castTemptWithBunnies();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Rabbit")).isEqualTo(2);
        assertThat(countPermanents(player2, "Rabbit")).isOne();
    }

    @Test
    @DisplayName("A declining opponent leaves only your own draw and Rabbit")
    void decliningOpponentLeavesOnlyTheMandatoryControllerDrawAndToken() {
        castTemptWithBunnies();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Rabbit")).isOne();
        assertThat(countPermanents(player2, "Rabbit")).isZero();
    }

    @Test
    @DisplayName("No opponent draws or creates a Rabbit until every opponent has decided")
    void opponentsDecideBeforeAnyoneDrawsOrCreates() {
        Player player3 = addThirdPlayer();
        harness.setHand(player1, List.of(new TemptWithBunnies()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLibrary(player3, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        for (int i = 0; i < 20 && !gd.interaction.isAwaitingInput(); i++) {
            UUID priorityId = gqs.getPriorityPlayerId(gd);
            Player priorityPlayer = List.of(player1, player2, player3).stream()
                    .filter(player -> player.getId().equals(priorityId)).findFirst().orElseThrow();
            harness.passPriority(priorityPlayer);
        }

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player2, "Rabbit")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.handleMayAbilityChosen(player3, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player3.getId())).hasSize(1);
        assertThat(countPermanents(player2, "Rabbit")).isOne();
        assertThat(countPermanents(player3, "Rabbit")).isOne();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(countPermanents(player1, "Rabbit")).isEqualTo(3);
    }

    private Player addThirdPlayer() {
        UUID id = UUID.randomUUID();
        Player player = new Player(id, "Charlie");
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), id, "Charlie");
        return player;
    }

    private void castTemptWithBunnies() {
        harness.setHand(player1, List.of(new TemptWithBunnies()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
