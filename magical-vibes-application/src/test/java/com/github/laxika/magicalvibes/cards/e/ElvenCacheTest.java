package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvenCache.class, Everglades.class})
class ElvenCacheTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Elven Cache puts a graveyard-targeted spell on the stack")
    void castingPutsGraveyardTargetedSpellOnStack() {
        Card target = new Everglades();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ElvenCache()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Resolve Elven Cache returns targeted card to hand")
    void resolvesAndReturnsTargetedCard() {
        Card target = new Everglades();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ElvenCache()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Elven Cache");
    }

    @Test
    @DisplayName("Elven Cache cannot target card in opponent graveyard")
    void cannotTargetCardInOpponentGraveyard() {
        Card opponentsCard = new Everglades();
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new ElvenCache()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentsCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Elven Cache cannot be cast without a card in your graveyard")
    void cannotCastWithoutGraveyardCard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new ElvenCache()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Elven Cache fizzles if targeted card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card target = new Everglades();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ElvenCache()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
