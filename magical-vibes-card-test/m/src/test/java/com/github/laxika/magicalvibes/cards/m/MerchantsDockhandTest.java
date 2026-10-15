package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerchantsDockhand.class, Ornithopter.class, AegisAutomaton.class, Shock.class})
class MerchantsDockhandTest extends BaseCardTest {

    @Test
    @DisplayName("Taps X artifacts and looks at X cards, putting one into hand")
    void tapsArtifactsAndLooksAtMatchingNumberOfCards() {
        Permanent dockhand = addReadyDockhand();
        Permanent artifact1 = addReadyArtifact();
        Permanent artifact2 = addReadyArtifact();
        Permanent artifact3 = addReadyArtifact();
        Card topCard = new AegisAutomaton();
        Card chosenCard = new Shock();
        Card thirdCard = new Ornithopter();
        harness.setLibrary(player1, List.of(topCard, chosenCard, thirdCard));

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
        gs.handleInteractionAnswer(gd, player1,
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

    @Test
    void zeroArtifactsStillPaysManaAndTapsDockhandWithoutMovingCards() {
        Permanent dockhand = addReadyDockhand();
        Card card = new Shock();
        harness.setLibrary(player1, List.of(card));
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(dockhand.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void looksAtAllAvailableCardsWhenLibraryIsShorterThanX() {
        addReadyDockhand();
        addReadyArtifact();
        addReadyArtifact();
        Card card = new Shock();
        harness.setLibrary(player1, List.of(card));
        addActivationMana();

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickArtifactCreatureCanPayAdditionalTapCost() {
        addReadyDockhand();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setSummoningSick(true);
        Card card = new Shock();
        harness.setLibrary(player1, List.of(card));
        addActivationMana();

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    @Test
    void tappedAndOpposingArtifactsCannotPayAdditionalTapCost() {
        Permanent dockhand = addReadyDockhand();
        Permanent tappedArtifact = addReadyArtifact();
        tappedArtifact.tap();
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dockhand.isTapped()).isFalse();
        assertThat(opposingArtifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickDockhandCannotActivateEvenWithZeroAdditionalArtifacts() {
        Permanent dockhand = harness.addToBattlefieldAndReturn(player1, new MerchantsDockhand());
        dockhand.setSummoningSick(true);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(dockhand.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unchosenCardsGoBelowUntouchedLibraryInChosenOrder() {
        addReadyDockhand();
        addReadyArtifact();
        addReadyArtifact();
        addReadyArtifact();
        Card first = new AegisAutomaton();
        Card chosen = new Shock();
        Card third = new Ornithopter();
        Card untouched = new MerchantsDockhand();
        harness.setLibrary(player1, List.of(first, chosen, third, untouched));
        addActivationMana();

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(reorder.cards().indexOf(third), reorder.cards().indexOf(first))));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, first);
    }

    private Permanent addReadyDockhand() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new MerchantsDockhand());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyArtifact() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
