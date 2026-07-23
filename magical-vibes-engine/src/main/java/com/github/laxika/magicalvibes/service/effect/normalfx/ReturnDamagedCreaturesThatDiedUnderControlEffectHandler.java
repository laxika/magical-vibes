package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamagedCreaturesThatDiedUnderControlEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves Krovikan Vampire's end-step return: each creature card this source damaged that died
 * this turn and is still continuously in a graveyard enters under the ability's controller,
 * linked for the control-loss sacrifice.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnDamagedCreaturesThatDiedUnderControlEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDamagedCreaturesThatDiedUnderControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }
        Set<UUID> cardIds = gameData.creatureCardsDamagedBySourceThatDiedThisTurn.get(sourcePermanentId);
        if (cardIds == null || cardIds.isEmpty()) {
            return;
        }

        // Snapshot — returning removes from the tracking set via leave-graveyard cleanup.
        for (UUID cardId : new ArrayList<>(cardIds)) {
            UUID ownerId = null;
            Card cardToReturn = null;
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Card> graveyard = gameData.playerGraveyards.get(pid);
                if (graveyard == null) continue;
                for (Card card : graveyard) {
                    if (card.getId().equals(cardId)) {
                        cardToReturn = card;
                        ownerId = pid;
                        break;
                    }
                }
                if (cardToReturn != null) break;
            }
            if (cardToReturn == null) {
                cardIds.remove(cardId);
                continue;
            }
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, cardToReturn,
                    com.github.laxika.magicalvibes.model.Zone.GRAVEYARD)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        GameLog.cardThen(cardToReturn, " can't return from the graveyard; it stays in the graveyard."));
                continue;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, cardToReturn.getId());
            Permanent permanent = new Permanent(cardToReturn);
            permanent.setEnteredFromGraveyardOwnerId(ownerId);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);

            if (!entry.getControllerId().equals(ownerId)) {
                gameData.stolenCreatures.put(permanent.getId(), ownerId);
                creatureControlService.applyControlEffect(gameData, entry.getControllerId(), permanent,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());
            }

            String playerName = gameData.playerIdToName.get(entry.getControllerId());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(cardToReturn,
                    " returns to the battlefield under " + playerName + "'s control."));
            log.info("Game {} - {} returns under {}'s control ({})",
                    gameData.id, cardToReturn.getName(), playerName, entry.getCard().getName());
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, entry.getControllerId(), cardToReturn, null, false);

            // Link for control-loss sacrifice only while the source is still on the battlefield.
            if (gameQueryService.findPermanentById(gameData, sourcePermanentId) != null) {
                gameData.seraphReturnedCreatures
                        .computeIfAbsent(sourcePermanentId, k -> ConcurrentHashMap.newKeySet())
                        .add(permanent.getId());
                gameData.seraphControlWatch.putIfAbsent(sourcePermanentId, entry.getControllerId());
            }
        }
    }
}
