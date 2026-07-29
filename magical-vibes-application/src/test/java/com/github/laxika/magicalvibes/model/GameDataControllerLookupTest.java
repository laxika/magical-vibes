package com.github.laxika.magicalvibes.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameDataControllerLookupTest {

    @Test
    void findsControllerByPermanentIdAcrossPermanentCopies() {
        UUID controllerId = UUID.randomUUID();
        GameData gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Player 1");
        gameData.orderedPlayerIds.add(controllerId);

        Permanent battlefieldPermanent = new Permanent(new Card());
        gameData.playerBattlefields.put(controllerId, gameData.newBattlefieldList());
        gameData.playerBattlefields.get(controllerId).add(battlefieldPermanent);

        Permanent permanentCopy = new Permanent(battlefieldPermanent);

        assertThat(gameData.findControllerOf(permanentCopy)).isEqualTo(controllerId);
    }
}
