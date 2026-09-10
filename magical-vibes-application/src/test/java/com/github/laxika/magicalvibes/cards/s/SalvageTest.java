package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Salvage.class, AlabornTrooper.class})
class SalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts a graveyard-targeted sorcery on the stack")
    void castingPutsGraveyardTargetedSorceryOnStack() {
        Card target = new AlabornTrooper();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Salvage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Resolving puts targeted card from own graveyard on top of own library")
    void resolvePutsCardOnTopOfOwnLibrary() {
        Card target = new AlabornTrooper();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Salvage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Puts the targeted card above the existing cards in its owner's library")
    void putsTargetAboveExistingLibraryCards() {
        Card target = new AlabornTrooper();
        Card existingTop = new AlabornTrooper();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(existingTop));
        harness.setHand(player1, List.of(new Salvage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(target.getId(), existingTop.getId());
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card opponentCard = new AlabornTrooper();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new Salvage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentCard.getId());
    }

    @Test
    @DisplayName("Fizzles if targeted card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card target = new AlabornTrooper();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Salvage()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
