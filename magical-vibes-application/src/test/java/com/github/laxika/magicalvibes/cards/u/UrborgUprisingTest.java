package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrborgUprising.class, Forest.class, GrizzlyBears.class})
class UrborgUprisingTest extends BaseCardTest {

    @Test
    void returnsUpToTwoCreaturesAndDrawsACard() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new GrizzlyBears();
        Card drawnCard = new Forest();
        harness.setGraveyard(player1, List.of(creature1, creature2, new Forest()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new UrborgUprising()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(creature1.getId(), creature2.getId(), drawnCard.getId());
        harness.assertInGraveyard(player1, "Urborg Uprising");
    }

    @Test
    void onlyCreatureCardsCanBeReturned() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new UrborgUprising()));
        addMana();

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    void choosingNoCreaturesStillDrawsACard() {
        Card creature = new GrizzlyBears();
        Card drawnCard = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new UrborgUprising()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactly(drawnCard.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
