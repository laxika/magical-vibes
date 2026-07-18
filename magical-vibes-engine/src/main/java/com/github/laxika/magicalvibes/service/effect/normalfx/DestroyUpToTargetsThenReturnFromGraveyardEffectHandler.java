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
import com.github.laxika.magicalvibes.model.effect.DestroyUpToTargetsThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestroyUpToTargetsThenReturnFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyUpToTargetsThenReturnFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        List<UUID> targetIds = entry.getTargetIds();
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        List<Card> cardsToReturn = new ArrayList<>();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            Card card = target.getCard();
            if (permanentRemovalService.tryDestroyPermanent(gameData, target, false)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " is destroyed."));
                log.info("Game {} - {} is destroyed by {}", gameData.id, card.getName(), sourceName);
                cardsToReturn.add(card);
            }
        }

        for (Card card : cardsToReturn) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
            if (graveyardOwnerId == null) {
                continue;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());

            if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                gameData.playerGraveyards.computeIfAbsent(graveyardOwnerId, k -> new ArrayList<>()).add(card);
                String blockedLog = gameData.playerIdToName.get(controllerId) + " can't put " + card.getName()
                        + " onto the battlefield from a graveyard; it stays in the graveyard.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(gameData.playerIdToName.get(controllerId) + " can't put ").card(card).text(" onto the battlefield from a graveyard; it stays in the graveyard.").build());
                log.info("Game {} - {} blocked from entering the battlefield from a graveyard",
                        gameData.id, card.getName());
                continue;
            }

            Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
            Permanent permanent = new Permanent(card);
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
            }
            permanent.setEnteredFromGraveyardOwnerId(controllerId);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " puts ", card, " onto the battlefield from a graveyard."));
            log.info("Game {} - {} returns {} to the battlefield under {}", gameData.id, playerName,
                    card.getName(), playerName);

            graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, permanent, card);
        }
    }
}
