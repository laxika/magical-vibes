package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LookAtHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Reveals hand contents to controller")
    void revealsHandToController() {
        Card card = createCard("Telepathy");
        LookAtHandEffect effect = new LookAtHandEffect();
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
        Card handCard = createCard("Mountain");
        gd.playerHands.get(player2Id).add(handCard);

        resolveEffect(gd, entry, effect);

        verify(cardRevealService).lookAtHand(gd, player1Id, player2Id);
    }

    @Test
    @DisplayName("Handles empty hand")
    void handlesEmptyHand() {
        Card card = createCard("Telepathy");
        LookAtHandEffect effect = new LookAtHandEffect();
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

        resolveEffect(gd, entry, effect);

        verify(cardRevealService).lookAtHand(gd, player1Id, player2Id);
    }

    @Test
    @DisplayName("Looks at the hand of the targeted spell's controller")
    void looksAtSpellControllersHand() {
        Card sourceCard = createCard("Lay Bare");
        Card targetSpellCard = createCard("Lightning Bolt");
        gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, targetSpellCard,
                player2Id, targetSpellCard.getName(), List.of()));
        LookAtHandEffect effect = new LookAtHandEffect(Subjects.controllerOf(Subjects.targetSpell()));
        StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), targetSpellCard.getId());

        resolveEffect(gd, entry, effect);

        verify(cardRevealService).lookAtHand(gd, player1Id, player2Id);
    }

    @Test
    @DisplayName("Looks at no hand when the targeted spell is gone and was not countered")
    void looksAtNoHandWhenTargetSpellIsGone() {
        Card sourceCard = createCard("Lay Bare");
        LookAtHandEffect effect = new LookAtHandEffect(Subjects.controllerOf(Subjects.targetSpell()));
        StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), UUID.randomUUID());

        resolveEffect(gd, entry, effect);

        verify(cardRevealService, never()).lookAtHand(any(), any(), any());
    }
}
