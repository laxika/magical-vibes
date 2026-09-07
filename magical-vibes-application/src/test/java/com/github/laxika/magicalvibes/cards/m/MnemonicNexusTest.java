package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MnemonicNexus.class, GiantSpider.class, GrizzlyBears.class})
class MnemonicNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles their graveyard into their library")
    void eachPlayerShufflesTheirGraveyardIntoTheirLibrary() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GiantSpider()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new GiantSpider(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GiantSpider()));
        harness.setHand(player1, List.of(new MnemonicNexus()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Mnemonic Nexus");
        assertThat(gameData.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gameData.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gameData.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Giant Spider", "Grizzly Bears");
        assertThat(gameData.playerDecks.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Giant Spider");
    }
}
