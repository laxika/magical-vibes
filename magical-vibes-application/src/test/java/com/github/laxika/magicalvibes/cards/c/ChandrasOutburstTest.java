package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChandrasOutburstTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct effects")
    void hasCorrectEffects() {
        ChandrasOutburst card = new ChandrasOutburst();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(SearchLibraryAndOrGraveyardForNamedCardToHandEffect.class);
    }

    @Test
    @DisplayName("Search effect targets Chandra, Bold Pyromancer by name")
    void searchEffectTargetsCorrectName() {
        ChandrasOutburst card = new ChandrasOutburst();

        SearchLibraryAndOrGraveyardForNamedCardToHandEffect searchEffect =
                (SearchLibraryAndOrGraveyardForNamedCardToHandEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(searchEffect.cardName()).isEqualTo("Chandra, Bold Pyromancer");
    }

    @Test
    @DisplayName("Deals 4 damage to target player")
    void deals4DamageToTargetPlayer() {
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Finds named card in graveyard and puts it into hand")
    void findsNamedCardInGraveyard() {
        Card chandraBold = new Card();
        chandraBold.setName("Chandra, Bold Pyromancer");
        chandraBold.setType(CardType.PLANESWALKER);
        chandraBold.setManaCost("{4}{R}{R}");
        chandraBold.setColor(CardColor.RED);

        harness.setGraveyard(player1, List.of(chandraBold));
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Damage dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Named card moved from graveyard to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chandra, Bold Pyromancer"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Chandra, Bold Pyromancer"));
    }

    @Test
    @DisplayName("Does not find named card when not in graveyard or library")
    void doesNotFindNamedCard() {
        harness.setHand(player1, List.of(new ChandrasOutburst()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Damage still dealt
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // No card found
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
