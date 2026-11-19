package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.BattlefieldEntryRequest;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BattlefieldEntryServiceTest {

    private final BattlefieldPlacementService placementService = mock(BattlefieldPlacementService.class);
    private final AsEntersInteractionService interactionService = mock(AsEntersInteractionService.class);
    private final EtbTriggerService triggerService = mock(EtbTriggerService.class);
    private final BattlefieldEntryService service =
            new BattlefieldEntryService(placementService, interactionService, triggerService);

    @Test
    @DisplayName("A land that names a card as it enters waits for the name before it is placed")
    void landNameChoicePausesPlacementUntilNameIsChosen() {
        var input = mock(com.github.laxika.magicalvibes.service.input.PlayerInputService.class);
        var entryService = new BattlefieldEntryService(placementService, interactionService, triggerService, input);
        GameData gameData = mock(GameData.class);
        UUID controllerId = UUID.randomUUID();
        Card land = new Card();
        land.setType(com.github.laxika.magicalvibes.model.CardType.LAND);
        land.addStaticEffect(new com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect(
                        com.github.laxika.magicalvibes.model.CardType.LAND));
        Permanent permanent = new Permanent(land);
        org.mockito.Mockito.when(input.beginCardNameChoice(gameData, controllerId, land,
                java.util.List.of(), false, false, null, com.github.laxika.magicalvibes.model.CardType.LAND,
                com.github.laxika.magicalvibes.model.Zone.HAND, permanent)).thenReturn(true);

        entryService.putLandOntoBattlefield(gameData, controllerId, permanent,
                com.github.laxika.magicalvibes.model.Zone.HAND);

        org.mockito.Mockito.verifyNoInteractions(placementService);
    }

    @Test
    @DisplayName("Default placement snapshots the enter-tapped types and delegates to the placement workflow")
    void delegatesDefaultPlacementToPlacementWorkflow() {
        GameData gameData = mock(GameData.class);
        UUID controllerId = UUID.randomUUID();
        Permanent permanent = mock(Permanent.class);

        service.putPermanentOntoBattlefield(gameData, controllerId, permanent);

        verify(placementService).snapshotEnterTappedTypes(gameData);
        verify(placementService).place(eq(gameData), any(BattlefieldEntryRequest.class));
    }

    @Test
    @DisplayName("Entry interaction handling is delegated to the interaction workflow")
    void delegatesEntryInteractionToInteractionWorkflow() {
        GameData gameData = mock(GameData.class);
        UUID controllerId = UUID.randomUUID();
        Card card = mock(Card.class);

        service.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);

        verify(interactionService).handleCreatureEnteredBattlefield(
                gameData, controllerId, card, null, false);
    }

    @Test
    @DisplayName("Enters-the-battlefield trigger collection is delegated to the trigger workflow")
    void delegatesEtbCollectionToTriggerWorkflow() {
        GameData gameData = mock(GameData.class);
        UUID controllerId = UUID.randomUUID();
        Card card = mock(Card.class);

        service.processCreatureETBEffects(gameData, controllerId, card, null, false);

        verify(triggerService).processCreatureETBEffects(
                gameData, controllerId, card, null, false);
    }
}
