package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspirationFromBeyond.class, LlanowarElves.class, Opt.class, DragonFodder.class,
        Forest.class, Plains.class})
class InspirationFromBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards, then returns a milled instant or sorcery to hand")
    void millsThenReturnsMilledSpell() {
        Card instant = new Opt();
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), instant));
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new InspirationFromBeyond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

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
        Card creature = new LlanowarElves();
        Card sorcery = new DragonFodder();
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Plains()));
        harness.setGraveyard(player1, List.of(creature, sorcery));
        harness.setHand(player1, List.of(new InspirationFromBeyond()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

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
        Card instant = new Opt();
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Plains()));
        harness.setGraveyard(player1, List.of(inspiration, instant));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, indexOfCard(player1, instant));

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(inspiration);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(inspiration);
    }


    @Test
    @DisplayName("Returning a spell is mandatory when a qualifying card is available")
    void cannotDeclineReturn() {
        Card instant = new Opt();
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Plains()));
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new InspirationFromBeyond()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, indexOfCard(player1, instant));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
    }

    @Test
    @DisplayName("A short library is milled completely and an existing spell can be returned")
    void shortLibraryStillReturnsExistingSpell() {
        Card land = new Plains();
        Card sorcery = new DragonFodder();
        Card opponentSpell = new Opt();
        Card opponentLibraryCard = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setGraveyard(player2, List.of(opponentSpell));
        harness.setHand(player1, List.of(new InspirationFromBeyond()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, indexOfCard(player1, sorcery));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentSpell);
    }

    @Test
    @DisplayName("With no qualifying cards the spell finishes and cannot return itself")
    void noQualifyingCardsFinishesWithoutReturningItself() {
        Card inspiration = new InspirationFromBeyond();
        Card creature = new LlanowarElves();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(land));
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(inspiration));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, land, inspiration);
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
