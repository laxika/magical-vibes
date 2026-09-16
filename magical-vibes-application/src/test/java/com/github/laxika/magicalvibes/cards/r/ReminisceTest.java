package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reminisce.class, GrizzlyBears.class, GiantSpider.class})
class ReminisceTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Reminisce puts it on the stack")
    void castingPutsItOnStack() {
        Card reminisce = new Reminisce();
        harness.setHand(player1, List.of(reminisce));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(reminisce);
        assertThat(entry.getTargetId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — target self =====

    @Test
    @DisplayName("Shuffles own graveyard into library")
    void shufflesOwnGraveyardIntoLibrary() {
        Card bear1 = new GrizzlyBears();
        Card bear2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear1, bear2));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        // The bears leave the graveyard; Reminisce itself goes there after resolution.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(bear1.getId(), bear2.getId());
        // Deck size should increase by 2 (the two bears from graveyard)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 2);
        // Bears should be in library
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bear1.getId(), bear2.getId());
        // Log confirms shuffle
        assertThat(gameLogContains("shuffles their graveyard")).isTrue();
    }

    // ===== Resolving — target opponent =====

    @Test
    @DisplayName("Can target opponent to shuffle their graveyard into their library")
    void canTargetOpponent() {
        Card bear = new GrizzlyBears();
        Card giant = new GiantSpider();
        Card reminisce = new Reminisce();
        harness.setGraveyard(player2, List.of(bear, giant));
        harness.setHand(player1, List.of(reminisce));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        // Opponent's graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .doesNotContain(bear.getId(), giant.getId());
        // Opponent's deck should grow by 2
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 2)
                .extracting(Card::getId)
                .contains(bear.getId(), giant.getId());
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Resolving with empty graveyard still shuffles library")
    void emptyGraveyardStillShufflesLibrary() {
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        // Deck size unchanged
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        // Log indicates empty graveyard
        assertThat(gameLogContains("graveyard is empty")).isTrue();
    }

    @Test
    @DisplayName("Reminisce itself goes to graveyard after resolution")
    void reminisceGoesToGraveyardAfterResolution() {
        Card reminisce = new Reminisce();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(reminisce));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        // Reminisce resolves first, shuffling the empty graveyard, then goes to the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(reminisce.getId());
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(harness.getGameData().stack).isEmpty();
    }
}

