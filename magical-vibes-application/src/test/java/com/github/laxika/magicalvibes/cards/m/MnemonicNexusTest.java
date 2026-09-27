package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MnemonicNexus.class, BorosRecruit.class, BorosSwiftblade.class})
class MnemonicNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles their graveyard into their library")
    void eachPlayerShufflesTheirGraveyardIntoTheirLibrary() {
        harness.setLibrary(player1, List.of(new BorosRecruit(), new BorosSwiftblade()));
        harness.setLibrary(player2, List.of(new BorosRecruit()));
        harness.setGraveyard(player1, List.of(new BorosSwiftblade(), new BorosRecruit()));
        harness.setGraveyard(player2, List.of(new BorosSwiftblade()));
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
                .contains("Boros Recruit", "Boros Swiftblade");
        assertThat(gameData.playerDecks.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Boros Swiftblade");
    }
}
