package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAndExileFromZonesEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChooseCardNameAndExileFromZonesEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Begins spell card name choice for controller")
    void beginsSpellCardNameChoice() {
        Card card = createCard("Slaughter Games");
        ChooseCardNameAndExileFromZonesEffect effect = new ChooseCardNameAndExileFromZonesEffect(List.of(CardType.LAND));
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginSpellCardNameChoice(gd, player1Id, player2Id, List.of(CardType.LAND), null);
    }

    @Test
    @DisplayName("Passes the required type through to the card name choice")
    void passesRequiredType() {
        Card card = createCard("Dispossess");
        ChooseCardNameAndExileFromZonesEffect effect = new ChooseCardNameAndExileFromZonesEffect(List.of(), CardType.ARTIFACT);
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginSpellCardNameChoice(gd, player1Id, player2Id, List.of(), CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Passes through the hand-exiled draw follow-up")
    void passesHandExiledDrawFollowUp() {
        Card card = createCard("Lost Legacy");
        ChooseCardNameAndExileFromZonesEffect effect =
                new ChooseCardNameAndExileFromZonesEffect(List.of(CardType.ARTIFACT, CardType.LAND), null, true);
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginSpellCardNameChoice(
                gd, player1Id, player2Id, List.of(CardType.ARTIFACT, CardType.LAND), null, true);
    }

    @Test
    @DisplayName("Passes a finite cap and hand-exile draw rider")
    void passesFiniteExileOptions() {
        Card card = createCard("The Stone Brain");
        ChooseCardNameAndExileFromZonesEffect effect =
                new ChooseCardNameAndExileFromZonesEffect(List.of(), null, 4, true);
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginSpellCardNameChoice(gd, player1Id, player2Id, List.of(), null, 4, true);
    }
}
