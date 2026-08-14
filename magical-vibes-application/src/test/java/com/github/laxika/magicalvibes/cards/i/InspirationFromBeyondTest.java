package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.a.AbandonedOutpost;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspirationFromBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards, then returns a milled instant or sorcery to hand")
    void millsThenReturnsMilledSpell() {
        Card instant = new HolyDay();
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), instant));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new InspirationFromBeyond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        int instantIndex = indexOfCard(player1, instant);
        harness.handleGraveyardCardChosen(player1, instantIndex);

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(instant);
    }

    @Test
    @DisplayName("Only instant and sorcery cards can be returned")
    void filtersReturnedCards() {
        Card creature = new GrizzlyBears();
        Card sorcery = new LavaAxe();
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new AbandonedOutpost()));
        harness.setGraveyard(player1, List.of(creature, sorcery));
        harness.setHand(player1, List.of(new InspirationFromBeyond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, indexOfCard(player1, creature)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleGraveyardCardChosen(player1, indexOfCard(player1, sorcery));

        assertThat(gd.playerHands.get(player1.getId())).contains(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Flashback mills three cards, returns a spell, and exiles Inspiration from Beyond")
    void flashbackResolvesAndExilesSpell() {
        Card inspiration = new InspirationFromBeyond();
        Card instant = new HolyDay();
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new AbandonedOutpost()));
        harness.setGraveyard(player1, List.of(inspiration, instant));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, indexOfCard(player1, instant));

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(inspiration);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(inspiration);
    }

    private int indexOfCard(com.github.laxika.magicalvibes.model.Player player, Card card) {
        List<Card> graveyard = gd.playerGraveyards.get(player.getId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getId().equals(card.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card not found in graveyard: " + card.getId());
    }
}
