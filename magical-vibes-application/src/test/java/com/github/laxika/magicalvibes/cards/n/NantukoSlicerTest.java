package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NantukoSlicer.class, GrizzlyBears.class, Shock.class})
class NantukoSlicerTest extends BaseCardTest {

    @Test
    void returnsTargetCardFromYourGraveyard() {
        Shock ownCard = new Shock();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setHand(player1, List.of(new NantukoSlicer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    void kickedAlsoConjuresAnAnyColorDuplicateFromAnOpponentsGraveyard() {
        Shock ownCard = new Shock();
        GrizzlyBears opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new NantukoSlicer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));

        PendingInteraction.MultiGraveyardChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(opponentChoice.validCardIds()).containsExactly(opponentCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears") && card.isTokenCard())
                .findFirst().orElseThrow();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);

        int duplicateIndex = gd.playerHands.get(player1.getId()).indexOf(duplicate);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, duplicateIndex);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
