package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PersistReturnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersistReturnEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PersistReturnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        Card card = entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            log.info("Game {} - {} persist return fizzles (no longer in a graveyard)", gameData.id, card.getName());
            return;
        }

        // Grafdigger's Cage etc.: creature cards in graveyards can't enter the battlefield, so the
        // persist return does nothing and the card stays in the graveyard.
        if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    card.getName() + " can't return from the graveyard (persist); it stays in the graveyard.");
            log.info("Game {} - {} persist return blocked (can't enter from a graveyard)", gameData.id, card.getName());
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(ownerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " returns " + card.getName() + " to the battlefield with a -1/-1 counter (persist).");
        log.info("Game {} - {} returns via persist with a -1/-1 counter", gameData.id, card.getName());

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, ownerId, permanent, card);
    }
}
