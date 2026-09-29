package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PerpetualCardCastCostSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Plunderer's Prize's conditional battlefield Seek. */
@Component
@RequiredArgsConstructor
public class SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var seek = (SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        int xValue = Math.max(0, entry.getXValue());
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> card.getManaValue() <= xValue)
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId))
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY))
                .toList());
        if (matchingCards.isEmpty()) {
            return;
        }

        Card sought = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.removeIf(card -> card.getId().equals(sought.getId()));

        Permanent permanent = new Permanent(sought, Zone.LIBRARY);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, controllerId, permanent,
                battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks and puts " + sought.getName() + " onto the battlefield.").build());
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, List.of(sought));

        if (sought.getManaValue() < xValue) {
            PerpetualCardCastCostSupport.rememberIncrease(
                    gameData, entry.getPhysicalCard(), seek.genericCastCostIncrease());
            entry.setReturnToHandAfterResolving(true);
        }
    }
}
