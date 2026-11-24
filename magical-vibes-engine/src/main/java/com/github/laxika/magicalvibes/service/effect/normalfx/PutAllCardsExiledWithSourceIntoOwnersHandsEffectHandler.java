package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutAllCardsExiledWithSourceIntoOwnersHandsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAllCardsExiledWithSourceIntoOwnersHandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutAllCardsExiledWithSourceIntoOwnersHandsEffect returnEffect =
                (PutAllCardsExiledWithSourceIntoOwnersHandsEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        if (sourcePermanentId == null && !returnEffect.anySourcePermanent()) {
            entry.setEventValue(0);
            return;
        }

        UUID sourceId = sourcePermanentId;
        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(exiled -> (returnEffect.anySourcePermanent() || sourceId.equals(exiled.sourcePermanentId()))
                        && (!returnEffect.onlyControllerOwned()
                        || entry.getControllerId().equals(exiled.ownerId()))
                        && (!returnEffect.onlyIntelCounters()
                        || gameData.exiledCardHasCounter(exiled.card().getId(), CounterType.INTEL)))
                .toList();

        int returnedCount = 0;
        for (ExiledCardEntry exiled : toReturn) {
            if (!gameData.removeFromExile(exiled.card().getId())) {
                continue;
            }
            returnedCount++;
            UUID ownerId = exiled.ownerId();
            gameData.addCardToHand(ownerId, exiled.card());
            // A card exiled face down goes to a hidden zone without being seen (CR 406.3).
            gameLogService.append(gameData, exiled.faceDown()
                    ? GameLog.text(gameData.playerIdToName.get(ownerId)
                    + " puts a face-down card from exile into their hand.")
                    : GameLog.textCardText(gameData.playerIdToName.get(ownerId) + " puts ", exiled.card(),
                    " from exile into their hand."));
            log.info("Game {} - {} returns {} from exile to its owner's hand via {}",
                    gameData.id, gameData.playerIdToName.get(ownerId), exiled.card().getName(), entry.getCard().getName());
        }
        entry.setEventValue(returnedCount);
    }
}
