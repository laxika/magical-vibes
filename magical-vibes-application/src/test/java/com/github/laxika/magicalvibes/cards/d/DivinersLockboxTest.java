package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Card topCard = createNamedCard("Named Card");
        Card secondCard = createNamedCard("Second Card");
        Card thirdCard = createNamedCard("Third Card");
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Named Card");

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
        Card topCard = createNamedCard("Actual Card");
        harness.setLibrary(player1, List.of(topCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activate();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Different Card");

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
        harness.handleListChoice(player1, "Any Card");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lockbox);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyLockbox() {
        Permanent lockbox = new Permanent(new DivinersLockbox());
        lockbox.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lockbox);
        return lockbox;
    }

    private void activate() {
        harness.activateAbility(player1, 0, 0, null, null);
    }

    private static Card createNamedCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        return card;
    }
}
