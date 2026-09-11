package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
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
    void landNameChoicePausesPlacementUntilNameIsChosen() {
        var input = mock(com.github.laxika.magicalvibes.service.input.PlayerInputService.class);
        var entryService = new BattlefieldEntryService(placementService, interactionService, triggerService, input);
        GameData gameData = mock(GameData.class);
        UUID controllerId = UUID.randomUUID();
        Card land = new Card();
        land.setType(com.github.laxika.magicalvibes.model.CardType.LAND);
        land.addEffect(com.github.laxika.magicalvibes.model.EffectSlot.ON_ENTER_BATTLEFIELD,
                new com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect(
                        com.github.laxika.magicalvibes.model.CardType.LAND));
        org.mockito.Mockito.when(input.beginCardNameChoice(gameData, controllerId, land,
                java.util.List.of(), false, false, null, com.github.laxika.magicalvibes.model.CardType.LAND,
                com.github.laxika.magicalvibes.model.Zone.HAND)).thenReturn(true);

        entryService.putLandOntoBattlefield(gameData, controllerId, new Permanent(land),
                com.github.laxika.magicalvibes.model.Zone.HAND);

        org.mockito.Mockito.verifyNoInteractions(placementService);
    }

    @Test
    void delegatesDefaultPlacementToPlacementWorkflow() {
        GameData gameData = mock(GameData.class);
        UUID controllerId = UUID.randomUUID();
        Permanent permanent = mock(Permanent.class);

        service.putPermanentOntoBattlefield(gameData, controllerId, permanent);

        verify(placementService).snapshotEnterTappedTypes(gameData);
        verify(placementService).place(eq(gameData), any(BattlefieldEntryRequest.class));
    }

    @Test
    void delegatesEntryInteractionToInteractionWorkflow() {
        GameData gameData = mock(GameData.class);
        UUID controllerId = UUID.randomUUID();
        Card card = mock(Card.class);

        service.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);

        verify(interactionService).handleCreatureEnteredBattlefield(
                gameData, controllerId, card, null, false);
    }

    @Test
    void delegatesEtbCollectionToTriggerWorkflow() {
        GameData gameData = mock(GameData.class);
        UUID controllerId = UUID.randomUUID();
        Card card = mock(Card.class);

        service.processCreatureETBEffects(gameData, controllerId, card, null, false);

        verify(triggerService).processCreatureETBEffects(
                gameData, controllerId, card, null, false);
    }
}
