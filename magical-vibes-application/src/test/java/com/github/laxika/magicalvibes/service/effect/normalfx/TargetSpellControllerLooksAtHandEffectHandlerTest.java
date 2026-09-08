package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLooksAtHandEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TargetSpellControllerLooksAtHandEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Looks at the hand of the targeted spell's controller")
    void looksAtSpellControllersHand() {
        Card sourceCard = createCard("Lay Bare");
        Card targetSpellCard = createCard("Lightning Bolt");
        gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, targetSpellCard,
                player2Id, targetSpellCard.getName(), List.of()));

        TargetSpellControllerLooksAtHandEffect effect = new TargetSpellControllerLooksAtHandEffect();
        StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), targetSpellCard.getId());

        resolveEffect(gd, entry, effect);

        verify(cardRevealService).lookAtHand(gd, player1Id, player2Id);
    }

    @Test
    @DisplayName("Does nothing when the target spell is no longer on the stack")
    void doesNothingWhenTargetNotOnStack() {
        Card sourceCard = createCard("Lay Bare");
        TargetSpellControllerLooksAtHandEffect effect = new TargetSpellControllerLooksAtHandEffect();
        StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), UUID.randomUUID());

        resolveEffect(gd, entry, effect);

        verify(cardRevealService, never()).lookAtHand(any(), any(), any());
    }
}
