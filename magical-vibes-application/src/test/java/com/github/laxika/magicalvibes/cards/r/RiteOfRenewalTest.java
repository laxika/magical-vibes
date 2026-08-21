package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfRenewal.class, GrizzlyBears.class, HolyDay.class, LeoninScimitar.class})
class RiteOfRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns permanent cards and shuffles target graveyard cards into its library")
    void returnsPermanentsAndShufflesTargetCards() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        Card opponentCard = new HolyDay();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));

        PendingInteraction.MultiGraveyardChoice shuffleChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(shuffleChoice.validCardIds()).containsExactly(opponentCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player2, "Holy Day");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize + 1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Renewal"));
    }

    @Test
    @DisplayName("Only permanent cards are legal for the return target group")
    void onlyPermanentCardsCanBeReturned() {
        Card permanent = new GrizzlyBears();
        Card instant = new HolyDay();
        Card opponentCard = new HolyDay();
        harness.setGraveyard(player1, List.of(permanent, instant));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice.validCardIds()).containsExactly(permanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Both up-to effects may choose zero cards")
    void bothEffectsMayChooseZeroCards() {
        harness.setHand(player1, List.of(new RiteOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Renewal"));
    }
}
