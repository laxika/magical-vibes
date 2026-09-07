package com.github.laxika.magicalvibes.cards.o;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnceAndFuture.class, Forest.class, GrizzlyBears.class})
class OnceAndFutureTest extends BaseCardTest {

    @Test
    void returnsOneCardToHandAndOneToLibraryWithoutAdamant() {
        Card returned = new GrizzlyBears();
        Card putOnTop = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(returned, putOnTop), false);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        PendingInteraction.MultiGraveyardChoice secondChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(secondChoice.validCardIds()).containsExactly(putOnTop.getId());
        harness.handleMultipleCardsChosen(player1, List.of(putOnTop.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(putOnTop);
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @Test
    void returnsBothCardsToHandWithAdamant() {
        Card returned = new GrizzlyBears();
        Card alsoReturned = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(returned, alsoReturned), true);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(alsoReturned.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(returned.getId())
                        || card.getId().equals(alsoReturned.getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    @Test
    void secondTargetIsOptionalAndMustBeDifferent() {
        Card returned = new GrizzlyBears();
        prepareSpell(List.of(returned), false);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(returned.getId()));
    }

    @Test
    void cannotBeCastWithoutAFirstTarget() {
        prepareSpell(List.of(), false);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillResolvesAndExilesWhenOneTargetBecomesIllegal() {
        Card returned = new GrizzlyBears();
        Card putOnTop = new Forest();
        OnceAndFuture spell = prepareSpell(List.of(returned, putOnTop), false);

        castAndBeginTargeting();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(putOnTop.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(returned);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(putOnTop);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(spell.getId()));
    }

    private OnceAndFuture prepareSpell(List<Card> graveyard, boolean adamant) {
        harness.setGraveyard(player1, graveyard);
        OnceAndFuture spell = new OnceAndFuture();
        harness.setHand(player1, List.of(spell));
        if (adamant) {
            harness.addMana(player1, ManaColor.GREEN, 4);
        } else {
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 3);
        }
        return spell;
    }

    private void castAndBeginTargeting() {
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
    }
}
