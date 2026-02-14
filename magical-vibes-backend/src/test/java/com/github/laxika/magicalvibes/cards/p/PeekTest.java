package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeekTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Peek has correct card properties")
    void hasCorrectProperties() {
        Peek card = new Peek();

        assertThat(card.getName()).isEqualTo("Peek");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(LookAtHandEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Peek puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, "U", 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Peek");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast Peek without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Peek()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Looking at hand =====

    @Test
    @DisplayName("Resolving Peek reveals opponent's hand in game log")
    void revealsOpponentHand() {
        Card cardInHand1 = new Peek();
        Card cardInHand2 = new Peek();
        harness.setHand(player2, List.of(cardInHand1, cardInHand2));

        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, "U", 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Peek"));
    }

    @Test
    @DisplayName("Resolving Peek against empty hand logs that hand is empty")
    void emptyHandLogged() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, "U", 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("Can target self to look at own hand")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, "U", 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    // ===== Drawing a card =====

    @Test
    @DisplayName("Resolving Peek draws a card")
    void drawsACard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, "U", 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Hand should have 1 card (Peek left hand, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Peek goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Peek()));
        harness.addMana(player1, "U", 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
    }
}
