package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringPermanentOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Mythweaver Poq's duplicates of the triggering lands, entering them together. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTriggeringPermanentOntoBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTriggeringPermanentOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> triggeringIds = !entry.getTriggeringPermanentIds().isEmpty()
                ? entry.getTriggeringPermanentIds()
                : entry.getTriggeringPermanentId() == null ? List.of() : List.of(entry.getTriggeringPermanentId());
        List<Card> sourceCards = new ArrayList<>();
        for (UUID triggeringId : triggeringIds) {
            Card sourceCard = triggeringCard(gameData, entry, triggeringId);
            if (sourceCard != null && !sourceCard.isToken()) {
                sourceCards.add(sourceCard);
            }
        }
        if (sourceCards.isEmpty()) return;

        // "For each of them, conjure a duplicate": the duplicates enter as one simultaneous event.
        UUID controllerId = entry.getControllerId();
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> entered = new ArrayList<>();
        boolean alreadyCollectingEntries = gameData.collectingSimultaneousCreatureEntryTriggers;
        gameData.collectingSimultaneousCreatureEntryTriggers = true;
        if (!alreadyCollectingEntries) {
            gameData.simultaneousCreatureEntryTriggers.clear();
        }
        try {
            for (Card sourceCard : sourceCards) {
                Card conjuredCard = sourceCard.createConjuredCopy();
                conjuredCard.setOwnerId(controllerId);
                Permanent permanent = new Permanent(conjuredCard);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent,
                        enterTappedTypes, List.copyOf(entered));
                triggerCollectionService.checkConjureTriggers(gameData, controllerId, 1);
                if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) continue;

                entered.add(permanent);
                entry.getCreatedPermanentIds().add(permanent.getId());
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        " conjures a duplicate of " + sourceCard.getName() + " onto the battlefield."));
            }
            for (Permanent permanent : entered) {
                Card conjuredCard = permanent.getCard();
                if (conjuredCard.hasType(CardType.LAND)) {
                    battlefieldEntryService.processLandETBEffects(gameData, controllerId, conjuredCard);
                } else {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(
                            gameData, controllerId, conjuredCard, null, false);
                }
            }
        } finally {
            gameData.collectingSimultaneousCreatureEntryTriggers = alreadyCollectingEntries;
            if (!alreadyCollectingEntries) {
                gameData.simultaneousCreatureEntryTriggers.clear();
            }
        }
    }

    private Card triggeringCard(GameData gameData, StackEntry entry, UUID triggeringId) {
        Permanent triggeringPermanent = gameQueryService.findPermanentById(gameData, triggeringId);
        if (triggeringPermanent != null) return triggeringPermanent.getCard();
        // The permanent left the battlefield before the trigger resolved: use its last-known
        // information, or the card it became in a graveyard.
        Card sourceCard = entry.lastKnownPermanentCard(triggeringId);
        if (sourceCard == null && triggeringId.equals(entry.getTriggeringPermanentId())
                && entry.getTriggeringCardId() != null) {
            sourceCard = gameQueryService.findCardInGraveyardById(gameData, entry.getTriggeringCardId());
        }
        return sourceCard;
    }
}
