package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NoviceOccultist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiregrafHorde.class, NoviceOccultist.class})
class DiregrafHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two decayed Zombies, then exiles up to two graveyard cards")
    void createsTokensThenExilesGraveyardCards() {
        Card ownCard = new NoviceOccultist();
        Card opponentCard = new NoviceOccultist();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new DiregrafHorde()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(zombies).hasSize(2).allSatisfy(zombie -> {
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
        });

        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId(), opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void canChooseNoGraveyardTargets() {
        Card card = new NoviceOccultist();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new DiregrafHorde()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsTokensWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new DiregrafHorde()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
        });
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canExileOnlyOneOfTwoAvailableCards() {
        Card chosenCard = new NoviceOccultist();
        Card unchosenCard = new NoviceOccultist();
        harness.setGraveyard(player2, List.of(chosenCard, unchosenCard));
        harness.setHand(player1, List.of(new DiregrafHorde()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unchosenCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosenCard);
    }

    @Test
    void exilesRemainingLegalTargetWhenAnotherTargetLeavesGraveyard() {
        Card removedCard = new NoviceOccultist();
        Card remainingCard = new NoviceOccultist();
        harness.setGraveyard(player2, List.of(removedCard, remainingCard));
        harness.setHand(player1, List.of(new DiregrafHorde()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(removedCard.getId(), remainingCard.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(removedCard, remainingCard);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player2, List.of(remainingCard));
        harness.setHand(player2, List.of(removedCard));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removedCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(remainingCard);
    }
}
