package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantsDockhandTest extends BaseCardTest {

    @Test
    @DisplayName("Taps X artifacts and looks at X cards, putting one into hand")
    void tapsArtifactsAndLooksAtMatchingNumberOfCards() {
        Permanent dockhand = addReadyDockhand();
        Permanent artifact1 = addReadyArtifact();
        Permanent artifact2 = addReadyArtifact();
        Permanent artifact3 = addReadyArtifact();
        Card topCard = new GrizzlyBears();
        Card chosenCard = new Shock();
        Card thirdCard = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(topCard, chosenCard, thirdCard));

        addActivationMana();
        harness.activateAbility(player1, 0, 3, null);

        assertThat(dockhand.isTapped()).isTrue();
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(artifact3.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(topCard, chosenCard, thirdCard);

        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.cards().indexOf(topCard), reorder.cards().indexOf(thirdCard))));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, thirdCard);
    }

    @Test
    @DisplayName("Cannot tap more artifacts than are untapped and controlled")
    void cannotActivateWithoutEnoughUntappedArtifacts() {
        Permanent dockhand = addReadyDockhand();
        addReadyArtifact();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dockhand.isTapped()).isFalse();
    }

    private Permanent addReadyDockhand() {
        Permanent permanent = new Permanent(new MerchantsDockhand());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyArtifact() {
        Permanent permanent = new Permanent(new Ornithopter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
