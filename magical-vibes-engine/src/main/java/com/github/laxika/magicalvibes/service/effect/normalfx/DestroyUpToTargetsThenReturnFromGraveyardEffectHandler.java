package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToTargetsThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
public class DestroyUpToTargetsThenReturnFromGraveyardEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyUpToTargetsThenReturnFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        List<UUID> targetIds = entry.getTargetIds();
        if ((targetIds == null || targetIds.isEmpty()) && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        List<Card> cardsToReturn = new ArrayList<>();

        List<Permanent> targets = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                targets.add(target);
            }
        }
        for (Permanent destroyed : destructionSupport.destroyBatchCollecting(gameData, targets, sourceName, false)) {
            Card originalCard = destroyed.getOriginalCard();
            if (!originalCard.isToken()
                    && (originalCard.hasType(CardType.CREATURE) || originalCard.hasType(CardType.PLANESWALKER))) {
                cardsToReturn.add(originalCard);
            }
        }

        List<Permanent> returnedPermanents = new ArrayList<>();
        for (Card card : cardsToReturn) {
            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
            if (graveyardOwnerId == null) {
                continue;
            }

            if (graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
                
                gameLogService.append(gameData, GameLog.builder().text(gameData.playerIdToName.get(controllerId) + " can't put ").card(card).text(" onto the battlefield from a graveyard; it stays in the graveyard.").build());
                log.info("Game {} - {} blocked from entering the battlefield from a graveyard",
                        gameData.id, card.getName());
                continue;
            }

            permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
            Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
            Permanent permanent = new Permanent(card);
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
            }
            permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes);

            if (((DestroyUpToTargetsThenReturnFromGraveyardEffect) effect).sacrificeAtEndStep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        permanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP,
                        false, null, controllerId, controllerId));
            }

            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", card, " onto the battlefield from a graveyard."));
            log.info("Game {} - {} returns {} to the battlefield under {}", gameData.id, playerName,
                    card.getName(), playerName);

            returnedPermanents.add(permanent);
        }

        boolean alreadyCollectingEntries = gameData.collectingSimultaneousCreatureEntryTriggers;
        gameData.collectingSimultaneousCreatureEntryTriggers = true;
        if (!alreadyCollectingEntries) {
            gameData.simultaneousCreatureEntryTriggers.clear();
        }
        try {
            for (Permanent permanent : returnedPermanents) {
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                        gameData, controllerId, permanent, permanent.getCard());
            }
        } finally {
            gameData.collectingSimultaneousCreatureEntryTriggers = alreadyCollectingEntries;
            if (!alreadyCollectingEntries) {
                gameData.simultaneousCreatureEntryTriggers.clear();
            }
        }
    }
}
