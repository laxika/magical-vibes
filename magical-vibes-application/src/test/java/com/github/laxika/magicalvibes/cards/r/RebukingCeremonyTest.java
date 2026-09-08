package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RebukingCeremonyTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Puts both target artifacts on top of their owners' libraries")
    void putsBothTargetArtifactsOnTopOfLibraries() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GoldMyr());
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");
        UUID goldMyrId = harness.getPermanentId(player2, "Gold Myr");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castSorcery(player1, 0, List.of(ornithopterId, goldMyrId));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId())).isEmpty();
        List<Card> deck = gameData.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 2);
        assertThat(deck.stream().limit(2).map(Card::getName))
                .containsExactlyInAnyOrder("Ornithopter", "Gold Myr");
    }

    @Test
    @DisplayName("Puts each artifact on top of its own owner's library")
    void putsArtifactsOnTheirOwnersLibraries() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GoldMyr());
        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");
        UUID goldMyrId = harness.getPermanentId(player2, "Gold Myr");
        int player1DeckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        int player2DeckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        harness.castSorcery(player1, 0, List.of(ornithopterId, goldMyrId));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Ornithopter");
        assertThat(gameData.playerDecks.get(player1.getId())).hasSize(player1DeckSizeBefore + 1);
        assertThat(gameData.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Gold Myr");
        assertThat(gameData.playerDecks.get(player2.getId())).hasSize(player2DeckSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");
        UUID grizzlyBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ornithopterId, grizzlyBearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same artifact twice")
    void cannotTargetSameArtifactTwice() {
        harness.addToBattlefield(player2, new Ornithopter());
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");

        harness.setHand(player1, List.of(new RebukingCeremony()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(ornithopterId, ornithopterId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
