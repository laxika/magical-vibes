package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivinersLockbox.class, Island.class})
class DivinersLockboxTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts the controller to name a card")
    void promptsControllerToNameCard() {
        addReadyLockbox();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ChooseCardNameRevealTopCardChoice.class);
    }

    @Test
    @DisplayName("A matching top card sacrifices Lockbox and draws three cards")
    void matchingTopCardSacrificesAndDrawsThree() {
        Permanent lockbox = addReadyLockbox();
        Card topCard = new Island();
        Card secondCard = new DivinersLockbox();
        Card thirdCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Island");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lockbox);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(lockbox.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(topCard.getId(), secondCard.getId(), thirdCard.getId());
    }

    @Test
    @DisplayName("A nonmatching top card stays on top and does not draw")
    void nonmatchingTopCardDoesNothing() {
        Permanent lockbox = addReadyLockbox();
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Diviner's Lockbox");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lockbox);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty library does not sacrifice Lockbox or draw")
    void emptyLibraryDoesNothing() {
        Permanent lockbox = addReadyLockbox();
        harness.setLibrary(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Island");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lockbox);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activation pays one mana and taps the artifact even on a miss")
    void activationPaysManaAndTaps() {
        Permanent lockbox = addReadyLockbox();
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate();
        assertThat(lockbox.isTapped()).isTrue();
        assertThatThrownBy(this::activate).isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Diviner's Lockbox");

        lockbox.untap();
        assertThatThrownBy(this::activate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation is forbidden outside the controller's main phase")
    void cannotActivateDuringUpkeep() {
        addReadyLockbox();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(this::activate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation is forbidden on another player's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyLockbox();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(this::activate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation is forbidden while the stack is nonempty")
    void cannotActivateWithNonemptyStack() {
        Permanent lockbox = addReadyLockbox();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activate();
        lockbox.untap();

        assertThatThrownBy(this::activate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A matching name still draws when the source has left the battlefield")
    void drawsEvenWhenSourceIsGone() {
        Permanent lockbox = addReadyLockbox();
        Card first = new Island();
        Card second = new Island();
        Card third = new DivinersLockbox();
        harness.setLibrary(player1, List.of(first, second, third));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activate();
        gd.playerBattlefields.get(player1.getId()).remove(lockbox);
        gd.playerGraveyards.get(player1.getId()).add(lockbox.getCard());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Island");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3)
                .contains(first, second, third);
    }

    @Test
    @DisplayName("Card-name suggestions must not disclose hidden hands or libraries")
    void nameSuggestionsDoNotDependOnHiddenCards() {
        Permanent lockbox = addReadyLockbox();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activate();
        harness.passBothPriorities();
        List<String> firstOptions = List.copyOf(gd.interaction
                .activeInteraction(PendingInteraction.ColorChoice.class).options());
        harness.handleListChoice(player1, "Diviner's Lockbox");

        harness.setHand(player2, List.of(new DivinersLockbox()));
        harness.setLibrary(player2, List.of(new DivinersLockbox()));
        lockbox.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activate();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyElementsOf(firstOptions);
    }

    private Permanent addReadyLockbox() {
        return harness.addToBattlefieldAndReturn(player1, new DivinersLockbox());
    }

    private void activate() {
        harness.activateAbility(player1, 0, 0, null, null);
    }
}
