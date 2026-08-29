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
