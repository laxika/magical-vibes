package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RebukingCeremony.class, DarksteelBrute.class, DarksteelIngot.class, CrazedGoblin.class})
class RebukingCeremonyTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Puts both target artifacts on top of their owners' libraries")
    void putsBothTargetArtifactsOnTopOfLibraries() {
        UUID darksteelBruteId = harness.addToBattlefieldAndReturn(player2, new DarksteelBrute()).getId();
        UUID darksteelIngotId = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot()).getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(darksteelBruteId, darksteelIngotId));

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId())).isEmpty();
        List<Card> deck = gameData.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 2);
        assertThat(deck.stream().limit(2).map(Card::getName))
                .containsExactlyInAnyOrder("Darksteel Brute", "Darksteel Ingot");
    }

    @Test
    @DisplayName("Puts each artifact on top of its own owner's library")
    void putsArtifactsOnTheirOwnersLibraries() {
        UUID darksteelBruteId = harness.addToBattlefieldAndReturn(player1, new DarksteelBrute()).getId();
        UUID darksteelIngotId = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot()).getId();
        int player1DeckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(darksteelBruteId, darksteelIngotId));

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Darksteel Brute");
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(player1DeckSizeBefore + 1);
        assertThat(gameData.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Darksteel Ingot");
        assertThat(gameData.playerDecks.get(player2.getId())).hasSize(player2DeckSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        UUID darksteelBruteId = harness.addToBattlefieldAndReturn(player2, new DarksteelBrute()).getId();
        UUID crazedGoblinId = harness.addToBattlefieldAndReturn(player2, new CrazedGoblin()).getId();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(darksteelBruteId, crazedGoblinId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same artifact twice")
    void cannotTargetSameArtifactTwice() {
        UUID darksteelBruteId = harness.addToBattlefieldAndReturn(player2, new DarksteelBrute()).getId();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(darksteelBruteId, darksteelBruteId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
