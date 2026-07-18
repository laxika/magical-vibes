package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameDataOpponentBattlefieldTest {

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    @DisplayName("getOpponentId returns the other seated player")
    void getOpponentIdReturnsOtherPlayer() {
        assertThat(gd.getOpponentId(player1Id)).isEqualTo(player2Id);
        assertThat(gd.getOpponentId(player2Id)).isEqualTo(player1Id);
    }

    @Test
    @DisplayName("getOpponentId returns null when no other player is seated")
    void getOpponentIdNullWhenAlone() {
        GameData alone = new GameData(UUID.randomUUID(), "solo", player1Id, "Solo");
        alone.orderedPlayerIds.add(player1Id);

        assertThat(alone.getOpponentId(player1Id)).isNull();
    }

    @Test
    @DisplayName("getOpponentBattlefield returns the opponent's permanents")
    void getOpponentBattlefieldReturnsOpponentPermanents() {
        Permanent bears = permanent("Bears");
        gd.playerBattlefields.get(player2Id).add(bears);
        gd.playerBattlefields.get(player1Id).add(permanent("Own"));

        assertThat(gd.getOpponentBattlefield(player1Id)).containsExactly(bears);
        assertThat(gd.getOpponentBattlefield(player2Id)).hasSize(1)
                .first().extracting(p -> p.getCard().getName()).isEqualTo("Own");
    }

    @Test
    @DisplayName("getOpponentBattlefield is empty when opponent has no battlefield list")
    void getOpponentBattlefieldEmptyWithoutList() {
        GameData bare = new GameData(UUID.randomUUID(), "bare", player1Id, "P1");
        bare.orderedPlayerIds.add(player1Id);
        bare.orderedPlayerIds.add(player2Id);

        assertThat(bare.getOpponentBattlefield(player1Id)).isEmpty();
    }

    @Test
    @DisplayName("getOpponentBattlefield is empty when there is no opponent")
    void getOpponentBattlefieldEmptyWhenAlone() {
        GameData alone = new GameData(UUID.randomUUID(), "solo", player1Id, "Solo");
        alone.orderedPlayerIds.add(player1Id);
        alone.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));

        assertThat(alone.getOpponentBattlefield(player1Id)).isEmpty();
    }

    private static Permanent permanent(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{G}");
        return new Permanent(card);
    }
}
