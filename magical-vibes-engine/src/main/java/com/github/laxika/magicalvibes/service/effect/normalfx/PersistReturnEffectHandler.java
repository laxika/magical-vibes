package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " can't return from the graveyard (persist); it stays in the graveyard."));
            log.info("Game {} - {} persist return blocked (can't enter from a graveyard)", gameData.id, card.getName());
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        // Vizier of Remedies replaces the persist -1/-1 counter (the creature isn't on the battlefield
        // yet, so use its owner as the entering controller); reduced to zero it re-persists.
        int persistCounters = gameQueryService.reduceMinusOneMinusOneCounters(gameData, ownerId, 1);
        permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, persistCounters);
        permanent.setEnteredFromGraveyardOwnerId(ownerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(ownerId);
        String persistSuffix = persistCounters > 0 ? " to the battlefield with a -1/-1 counter (persist)." : " to the battlefield (persist).";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " returns ", card, persistSuffix));
        log.info("Game {} - {} returns via persist with {} -1/-1 counter(s)", gameData.id, card.getName(), persistCounters);

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, ownerId, permanent, card);
    }
}
