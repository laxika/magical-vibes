package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TomeScourTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Tome Scour has correct card properties")
    void hasCorrectProperties() {
        TomeScour card = new TomeScour();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(MillTargetPlayerEffect.class);
        MillTargetPlayerEffect effect = (MillTargetPlayerEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(5);
    }

    // ===== Milling =====

    @Test
    @DisplayName("Mills five cards from target player's library")
    void millsFiveCards() {
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Milled cards come from the top of the library")
    void milledCardsFromTopOfLibrary() {
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 7) {
            deck.removeFirst();
        }
        Card topCard = deck.get(0);
        Card sixthCard = deck.get(5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(sixthCard);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        // 5 milled cards + Tome Scour itself goes to graveyard after resolving
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tome Scour"));
    }

    @Test
    @DisplayName("Mills only remaining cards when library has fewer than five")
    void millsOnlyRemainingWhenLibrarySmall() {
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 3) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills nothing when library is empty")
    void millsNothingWhenLibraryEmpty() {
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gd.playerDecks.get(player2.getId()).clear();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tome Scour goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tome Scour"));
        assertThat(gd.stack).isEmpty();
    }
}
